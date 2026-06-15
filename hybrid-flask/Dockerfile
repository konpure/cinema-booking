FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

HEALTHCHECK --interval=15s --timeout=5s --retries=3 \
    CMD curl -sf http://localhost:5000/health || exit 1

CMD ["gunicorn", "app:app", "-b", "0.0.0.0:5000", "--workers=2", "--timeout=30", "--access-logfile=-", "--error-logfile=-"]