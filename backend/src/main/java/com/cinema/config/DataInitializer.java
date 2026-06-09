package com.cinema.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.entity.Snack;
import com.cinema.entity.User;
import com.cinema.mapper.CinemaMapper;
import com.cinema.mapper.MovieMapper;
import com.cinema.mapper.ScreeningMapper;
import com.cinema.mapper.SnackMapper;
import com.cinema.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int MIN_SCREENINGS_PER_MOVIE_CINEMA = 4;

    private final UserMapper userMapper;
    private final CinemaMapper cinemaMapper;
    private final MovieMapper movieMapper;
    private final ScreeningMapper screeningMapper;
    private final SnackMapper snackMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        initUsers();
        initCinemas();
        patchLegacyScreenings();
        initMovies();
        enrichCatalog();
        initSnacks();
    }

    private void initUsers() {
        createUserIfAbsent("admin", "admin123", "ADMIN");
        createUserIfAbsent("demo", "demo123", "USER");
    }

    private void createUserIfAbsent(String username, String rawPassword, String role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count == 0) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("Created default user: {} / {}", username, rawPassword);
        }
    }

    private void initCinemas() {
        if (cinemaMapper.selectCount(null) > 0) return;
        cinema("星影国际影城（朝阳店）", "北京", "朝阳区建国路93号万达广场5层", "010-85888801", "chaoyang");
        cinema("星影激光影城（海淀店）", "北京", "海淀区中关村大街19号新中关B1", "010-62666602", "haidian");
        cinema("星影IMAX影城（西单店）", "北京", "西城区西单北大街131号大悦城8层", "010-66006603", "xidan");
        log.info("Initialized cinemas");
    }

    private void cinema(String name, String city, String address, String phone, String seed) {
        Cinema c = new Cinema();
        c.setName(name);
        c.setCity(city);
        c.setAddress(address);
        c.setPhone(phone);
        c.setCover("https://picsum.photos/seed/" + seed + "/800/400");
        c.setStatus("OPEN");
        cinemaMapper.insert(c);
    }

    private void patchLegacyScreenings() {
        jdbcTemplate.update("UPDATE screenings SET cinema_id = 1 WHERE cinema_id IS NULL");
    }

    private void initMovies() {
        if (movieMapper.selectCount(null) > 0) return;

        Movie m1 = movie("星际穿越", "科幻", 169, 9.4,
                "当地球面临灭绝，一队探险者穿越虫洞，寻找人类新家园。", "interstellar");
        Movie m2 = movie("奥本海默", "传记", 180, 8.9,
                "讲述原子弹之父罗伯特·奥本海默的传奇人生。", "oppenheimer");
        Movie m3 = movie("沙丘2", "科幻", 166, 9.1,
                "保罗·厄崔迪与弗雷曼人联手，向制造他悲剧的阴谋者复仇。", "dune2");

        movieMapper.insert(m1);
        movieMapper.insert(m2);
        movieMapper.insert(m3);

        screening(m1.getId(), 1L, "IMAX 1号厅", slot(0, 14, 0), 68);
        screening(m1.getId(), 2L, "激光 2号厅", slot(0, 17, 30), 48);
        screening(m2.getId(), 1L, "杜比 3号厅", slot(0, 19, 45), 58);
        screening(m2.getId(), 3L, "激光 1号厅", slot(0, 20, 15), 52);
        screening(m3.getId(), 2L, "IMAX 巨幕厅", slot(0, 15, 20), 78);
        screening(m3.getId(), 3L, "IMAX 1号厅", slot(0, 21, 0), 88);
        log.info("Initialized sample movies and screenings");
    }

    /** 启动时补齐影城、影片与场次，已有数据库也会增量更新 */
    private void enrichCatalog() {
        ensureExtraCinemas();

        List<MovieSpec> specs = List.of(
                new MovieSpec("星际穿越", "科幻", 169, 9.4,
                        "当地球面临灭绝，一队探险者穿越虫洞，寻找人类新家园。", "interstellar"),
                new MovieSpec("奥本海默", "传记", 180, 8.9,
                        "讲述原子弹之父罗伯特·奥本海默的传奇人生。", "oppenheimer"),
                new MovieSpec("沙丘2", "科幻", 166, 9.1,
                        "保罗·厄崔迪与弗雷曼人联手，向制造他悲剧的阴谋者复仇。", "dune2"),
                new MovieSpec("流浪地球2", "科幻", 173, 8.8,
                        "太阳危机逼近，人类开启流浪地球计划，拯救家园。", "wandering-earth-2"),
                new MovieSpec("封神第一部", "奇幻", 148, 8.5,
                        "商周交替之际，姜子牙携众神对抗纣王暴政。", "creation-god"),
                new MovieSpec("芭比", "喜剧", 114, 8.2,
                        "芭比与肯离开芭比乐园，探索真实世界的冒险。", "barbie"),
                new MovieSpec("铃芽之旅", "动画", 122, 8.7,
                        "少女铃芽与闭门师草太踏上关闭灾难之门的旅程。", "suzume"),
                new MovieSpec("年会不能停", "喜剧", 117, 8.4,
                        "集团裁员风波中，三位打工人误打误撞的职场逆袭。", "johnny-keep-walking"),
                new MovieSpec("坚如磐石", "犯罪", 127, 8.0,
                        "一场涉及政商关系的连环命案，揭开城市暗面。", "under-the-light"),
                new MovieSpec("志愿军：雄兵出击", "战争", 141, 8.3,
                        "抗美援朝战争中，志愿军浴血奋战的群像史诗。", "volunteers")
        );

        List<Movie> movies = specs.stream().map(this::ensureMovie).toList();
        List<Cinema> cinemas = cinemaMapper.selectList(new LambdaQueryWrapper<Cinema>().eq(Cinema::getStatus, "OPEN"));
        if (cinemas.isEmpty()) return;

        String[][] halls = {
                {"IMAX 1号厅", "激光 2号厅", "杜比 3号厅", "4DX 动感厅", "VIP 贵宾厅"},
                {"激光 1号厅", "激光 2号厅", "杜比全景声厅", "亲子厅", "CINITY 厅"},
                {"IMAX 巨幕厅", "激光 1号厅", "杜比 2号厅", "情侣厅", "4K 激光厅"},
                {"CINITY 1号厅", "激光 3号厅", "杜比全景声厅", "VIP 厅", "IMAX 激光厅"},
                {"4DX 1号厅", "激光 2号厅", "杜比 3号厅", "巨幕厅", "尊享厅"}
        };
        int[][] slots = {
                {13, 0}, {15, 30}, {18, 0}, {20, 30},
                {10, 30}, {14, 0}, {16, 45}, {21, 15}
        };
        int[] basePrices = {42, 48, 55, 62, 68, 75, 82, 88};

        int added = 0;
        for (int ci = 0; ci < cinemas.size(); ci++) {
            Cinema cinema = cinemas.get(ci);
            String[] cinemaHalls = halls[ci % halls.length];
            for (int mi = 0; mi < movies.size(); mi++) {
                Movie movie = movies.get(mi);
                long existing = screeningMapper.selectCount(new LambdaQueryWrapper<Screening>()
                        .eq(Screening::getMovieId, movie.getId())
                        .eq(Screening::getCinemaId, cinema.getId())
                        .eq(Screening::getStatus, "OPEN"));
                if (existing >= MIN_SCREENINGS_PER_MOVIE_CINEMA) continue;

                for (int si = (int) existing; si < MIN_SCREENINGS_PER_MOVIE_CINEMA; si++) {
                    int[] time = slots[si % slots.length];
                    int dayOffset = si >= 4 ? 1 : 0;
                    int price = basePrices[(ci + mi + si) % basePrices.length];
                    screening(
                            movie.getId(),
                            cinema.getId(),
                            cinemaHalls[si % cinemaHalls.length],
                            slot(dayOffset, time[0], time[1]),
                            price
                    );
                    added++;
                }
            }
        }
        if (added > 0) {
            log.info("Enriched catalog: {} cinemas, {} movies, added {} screenings", cinemas.size(), movies.size(), added);
        }
    }

    private void ensureExtraCinemas() {
        ensureCinema("星影 CINITY 影城（浦东店）", "上海", "浦东新区陆家嘴环路1000号正大广场7层", "021-58886601", "pudong");
        ensureCinema("星影 4DX 影城（天河店）", "广州", "天河区天河路208号天河城5层", "020-85556602", "tianhe");
    }

    private void ensureCinema(String name, String city, String address, String phone, String seed) {
        Long count = cinemaMapper.selectCount(new LambdaQueryWrapper<Cinema>().eq(Cinema::getName, name));
        if (count == 0) {
            cinema(name, city, address, phone, seed);
            log.info("Added cinema: {}", name);
        }
    }

    private Movie ensureMovie(MovieSpec spec) {
        Movie existing = movieMapper.selectOne(new LambdaQueryWrapper<Movie>().eq(Movie::getTitle, spec.title()));
        if (existing != null) return existing;
        Movie movie = movie(spec.title(), spec.genre(), spec.duration(), spec.rating(), spec.description(), spec.seed());
        movieMapper.insert(movie);
        log.info("Added movie: {}", spec.title());
        return movie;
    }

    private void initSnacks() {
        if (snackMapper.selectCount(null) > 0) return;
        snack("中桶爆米花", "SINGLE", 28, "现爆黄油爆米花，观影标配", "popcorn-m");
        snack("大桶爆米花", "SINGLE", 38, "超大份分享装", "popcorn-l");
        snack("可口可乐 500ml", "SINGLE", 12, "冰爽可乐", "coke");
        snack("零度可乐 500ml", "SINGLE", 12, "零糖零卡", "coke-zero");
        snack("情侣套餐", "COMBO", 48, "大爆米花 + 2杯可乐", "combo-couple");
        snack("家庭分享套餐", "COMBO", 68, "大爆米花 + 3杯可乐 + 鸡米花", "combo-family");
        log.info("Initialized snacks");
    }

    private Movie movie(String title, String genre, int duration, double rating, String desc, String seed) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setPoster("https://picsum.photos/seed/" + seed + "/400/600");
        m.setGenre(genre);
        m.setDuration(duration);
        m.setRating(BigDecimal.valueOf(rating));
        m.setDescription(desc);
        m.setStatus("SHOWING");
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }

    private LocalDateTime slot(int dayOffset, int hour, int minute) {
        LocalDateTime time = LocalDateTime.now()
                .plusDays(dayOffset)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
        while (time.isBefore(LocalDateTime.now().plusMinutes(20))) {
            time = time.plusDays(1);
        }
        return time;
    }

    private void screening(Long movieId, Long cinemaId, String hall, LocalDateTime startTime, int price) {
        Screening s = new Screening();
        s.setMovieId(movieId);
        s.setCinemaId(cinemaId);
        s.setHallName(hall);
        s.setStartTime(startTime);
        s.setPrice(BigDecimal.valueOf(price));
        s.setSeatRows(8);
        s.setSeatCols(12);
        s.setStatus("OPEN");
        screeningMapper.insert(s);
    }

    private void screening(Long movieId, Long cinemaId, String hall, int hoursLater, int price) {
        screening(movieId, cinemaId, hall, LocalDateTime.now().plusHours(hoursLater), price);
    }

    private void snack(String name, String category, int price, String desc, String seed) {
        Snack s = new Snack();
        s.setName(name);
        s.setCategory(category);
        s.setPrice(BigDecimal.valueOf(price));
        s.setDescription(desc);
        s.setImage("https://picsum.photos/seed/" + seed + "/200/200");
        s.setStatus("ON_SALE");
        snackMapper.insert(s);
    }

    private record MovieSpec(String title, String genre, int duration, double rating, String description, String seed) {}
}
