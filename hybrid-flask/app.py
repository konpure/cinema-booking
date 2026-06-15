import os
import subprocess
import json
from datetime import datetime
from flask import Flask, jsonify, request

app = Flask(__name__)

BASE_DIR = "/home/cinema/cinema-booking"
COMPOSE_FILE = os.path.join(BASE_DIR, "docker/docker-compose.yml")

SERVICES = {
    "mysql":     {"port": 3306, "host": "localhost"},
    "redis":     {"port": 6379, "host": "localhost"},
    "rabbitmq":  {"port": 5672, "host": "localhost"},
    "nacos":     {"port": 8848, "host": "localhost"},
    "backend-1": {"port": 8081, "host": "localhost"},
    "backend-2": {"port": 8082, "host": "localhost"},
    "nginx":     {"port": 80,   "host": "localhost"},
}

def check_port(host, port, timeout=3):
    cmd = f"timeout {timeout} bash -c 'echo > /dev/tcp/{host}/{port}' 2>/dev/null && echo UP || echo DOWN"
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return result.stdout.strip()

@app.route("/health")
def health():
    results = {}
    all_up = True
    for name, cfg in SERVICES.items():
        status = check_port(cfg["host"], cfg["port"])
        results[name] = status
        if status != "UP":
            all_up = False
    return jsonify({
        "status": "UP" if all_up else "DEGRADED",
        "timestamp": datetime.now().isoformat(),
        "services": results
    })

@app.route("/health/<service>")
def service_health(service):
    if service not in SERVICES:
        return jsonify({"error": f"Unknown service: {service}"}), 404
    cfg = SERVICES[service]
    status = check_port(cfg["host"], cfg["port"])
    return jsonify({"service": service, "status": status})

@app.route("/api/compose/ps", methods=["GET"])
def compose_ps():
    try:
        result = subprocess.run(
            ["docker", "compose", "-f", COMPOSE_FILE, "ps", "--format", "json"],
            capture_output=True, text=True, timeout=15, cwd=os.path.dirname(COMPOSE_FILE)
        )
        lines = [l for l in result.stdout.strip().split("\n") if l]
        containers = [json.loads(l) for l in lines] if lines else []
        return jsonify({"count": len(containers), "containers": containers})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/compose/logs/<service>", methods=["GET"])
def compose_logs(service):
    tail = request.args.get("tail", 50)
    try:
        result = subprocess.run(
            ["docker", "compose", "-f", COMPOSE_FILE, "logs", "--tail", str(tail), service],
            capture_output=True, text=True, timeout=15, cwd=os.path.dirname(COMPOSE_FILE)
        )
        return jsonify({"service": service, "logs": result.stdout.split("\n")})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/build", methods=["POST"])
def trigger_build():
    service = request.json.get("service", "all")
    try:
        if service == "all":
            cmd = ["docker", "compose", "-f", COMPOSE_FILE, "build"]
        else:
            cmd = ["docker", "compose", "-f", COMPOSE_FILE, "build", service]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300, cwd=os.path.dirname(COMPOSE_FILE))
        return jsonify({
            "success": result.returncode == 0,
            "output": result.stdout[-2000:] if result.stdout else "",
            "error": result.stderr[-2000:] if result.stderr else ""
        })
    except subprocess.TimeoutExpired:
        return jsonify({"error": "Build timed out"}), 504

@app.route("/api/backend/restart", methods=["POST"])
def restart_backend():
    try:
        for svc in ["backend-1", "backend-2"]:
            subprocess.run(
                ["docker", "compose", "-f", COMPOSE_FILE, "restart", svc],
                capture_output=True, timeout=30, cwd=os.path.dirname(COMPOSE_FILE)
            )
        return jsonify({"status": "ok", "message": "Backend services restarted"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/k8s/status", methods=["GET"])
def k8s_status():
    try:
        result = subprocess.run(
            ["kubectl", "get", "all", "-n", "cinema", "-o", "wide"],
            capture_output=True, text=True, timeout=15
        )
        return jsonify({"status": "ok", "output": result.stdout.split("\n")})
    except FileNotFoundError:
        return jsonify({"status": "unavailable", "message": "kubectl not installed"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)})

@app.route("/api/debug/env")
def debug_env():
    safe_keys = ["PATH", "HOME", "USER", "HOSTNAME", "PWD"]
    env = {k: v for k, v in os.environ.items() if k in safe_keys or k.startswith("CINEMA_")}
    for k in env:
        if any(kw in k.lower() for kw in ["pass", "secret", "key", "token"]):
            env[k] = env[k][:4] + "****"
    return jsonify(env)

if __name__ == "__main__":
    port = int(os.environ.get("FLASK_PORT", 5000))
    debug = os.environ.get("FLASK_DEBUG", "false").lower() == "true"
    app.run(host="0.0.0.0", port=port, debug=debug)
