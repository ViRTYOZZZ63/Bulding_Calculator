-- V1: Создание таблицы пользователей
CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       login       VARCHAR(100) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       first_name  VARCHAR(100),
                       last_name   VARCHAR(100),
                       role        VARCHAR(50)  NOT NULL DEFAULT 'MANAGER',
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- V2: Создание таблицы клиентов
CREATE TABLE clients (
                         id          BIGSERIAL PRIMARY KEY,
                         last_name   VARCHAR(100) NOT NULL,
                         first_name  VARCHAR(100) NOT NULL,
                         patronymic  VARCHAR(100),
                         phone       VARCHAR(20),
                         email       VARCHAR(150),
                         address     TEXT,
                         created_by  BIGINT REFERENCES users(id),
                         created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V3: Создание таблицы расчётов
CREATE TABLE calculations (
                              id                    BIGSERIAL PRIMARY KEY,
                              client_id             BIGINT    NOT NULL REFERENCES clients(id),
                              created_by            BIGINT    REFERENCES users(id),
                              construction_address  TEXT,
                              status                VARCHAR(50) NOT NULL DEFAULT 'ACTUAL',
    -- ACTUAL / NOT_ACTUAL / CONTRACT_SIGNED
                              prices_fixed_until    TIMESTAMP,
                              created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

-- V4: Создание таблицы конструктивных элементов
CREATE TABLE calculation_elements (
                                      id                BIGSERIAL PRIMARY KEY,
                                      calculation_id    BIGINT      NOT NULL REFERENCES calculations(id) ON DELETE CASCADE,
                                      element_type      VARCHAR(50) NOT NULL,  -- FRAME / FOUNDATION
                                      input_params      JSONB,
                                      created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
                                      updated_at        TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- V5: Создание таблицы строк результата расчёта
CREATE TABLE calculation_result_items (
                                          id                      BIGSERIAL   PRIMARY KEY,
                                          calculation_element_id  BIGINT      NOT NULL REFERENCES calculation_elements(id) ON DELETE CASCADE,
                                          section                 VARCHAR(200),   -- "Внешние стены / 1 этаж"
                                          material_name           VARCHAR(200),
                                          unit                    VARCHAR(50),
                                          quantity                NUMERIC(12,4),
                                          unit_price              NUMERIC(12,2),  -- зафиксировано на момент расчёта
                                          total_price             NUMERIC(14,2)
);

-- V6: Создание справочника материалов
CREATE TABLE materials (
                           id            BIGSERIAL    PRIMARY KEY,
                           name          VARCHAR(200) NOT NULL UNIQUE,
                           unit          VARCHAR(50),
                           current_price NUMERIC(12,2) NOT NULL,
                           updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- V7: Начальный справочник материалов
-- Источник: РАСЧЕТ_КАРКАСА.xlsx → лист "Справочник материалов и прайс"
--           свайный_фундамент.xlsx → лист "ЗАБИВНЫЕ СВАИ"

-- ===== Пиломатериал (цена за м3) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Доска 50*100*3000', 'м3', 12000),
                                                      ('Доска 50*150*3000', 'м3', 12000),
                                                      ('Доска 50*200*3000', 'м3', 12000),
                                                      ('Доска 50*250*3000', 'м3', 12000),
                                                      ('Доска 50*300*3000', 'м3', 12000),
                                                      ('Доска 50*100*6000', 'м3', 12000),
                                                      ('Доска 50*150*6000', 'м3', 12000),
                                                      ('Доска 50*200*6000', 'м3', 12000),
                                                      ('Доска 50*250*6000', 'м3', 12000),
                                                      ('Доска 50*300*6000', 'м3', 12000);

-- ===== Опалубка =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Доска 30*100*3000', 'м3', 7800),
                                                      ('Брус 50*50*3000',   'м3', 9100);

-- ===== ОСБ (цена за м2) =====
-- Продаётся листами 1.25×2.5=3.125 м², цена за лист / 3.125 = цена за м2
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('OSB 9 мм',  'м2', 256),   -- лист 800 / 3.125
                                                      ('OSB 10 мм', 'м2', 288),   -- лист 900 / 3.125
                                                      ('OSB 15 мм', 'м2', 384),   -- лист 1200 / 3.125
                                                      ('OSB 18 мм', 'м2', 480);   -- лист 1500 / 3.125

-- ===== Утеплитель (цена за м3) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Кнауф ТеплоКнауф 100 мм', 'м3', 3000),
                                                      ('Технониколь 100 мм',       'м3', 3500),
                                                      ('Эковер 100 мм',            'м3', 2800),
                                                      ('Эковер 150 мм',            'м3', 2800),
                                                      ('Эковер 200 мм',            'м3', 2800),
                                                      ('Фасад 200 мм',             'м3', 3200),
                                                      ('Эковер 250 мм',            'м3', 2800);

-- ===== Пароизоляция (цена за м2) =====
-- Продаётся рулонами, пересчёт: цена_рулона / м²_в_рулоне
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Ондутис',                                          'м2', 33.33),   -- рулон 75м², 2500 руб
                                                      ('Пароизоляция Axton (b)',                           'м2', 20.00),   -- рулон 70м², 1400 руб
                                                      ('Пароизоляционная пленка Ютафол Н 96 Сильвер',     'м2', 21.33),   -- рулон 75м², 1600 руб
                                                      ('Пароизоляция В',                                   'м2', 10.71);   -- рулон 70м², 750 руб

-- ===== Ветрозащита (цена за м2) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Ветро-влагозащитная мембрана Brane А',         'м2', 56.67),  -- рулон 30м², 1700 руб
                                                      ('Паропроницаемая ветро-влагозащита A Optima',   'м2', 21.33),  -- рулон 75м², 1600 руб
                                                      ('Гидро-ветрозащита Тип А',                      'м2', 53.33);  -- рулон 15м², 800 руб

-- ===== Сваи (цена за шт) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Бетонные сваи 150*150*3000', 'шт', 1500),
                                                      ('Бетонные сваи 200*200*3000', 'шт', 1900),
                                                      ('Бетонные сваи 300*300*3000', 'шт', 3383),
                                                      ('Бетонные сваи 300*300*4000', 'шт', 4346),
                                                      ('Бетонные сваи 300*300*5000', 'шт', 4953);

-- ===== Бетон (цена за м3) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('М150(В10)',   'м3', 2760),
                                                      ('М200(В15)',   'м3', 2910),
                                                      ('М250(В20)',   'м3', 3100),
                                                      ('М300 (В22.5)','м3', 3160),
                                                      ('М350(В25)',   'м3', 3260),
                                                      ('М400(В30)',   'м3', 3460),
                                                      ('М450(В35)',   'м3', 3680),
                                                      ('М500(В40)',   'м3', 3760),
                                                      ('М550(В45)',   'м3', 4135),
                                                      ('М600(В50)',   'м3', 4335),
                                                      ('М700(В55)',   'м3', 4680),
                                                      ('М800(В65)',   'м3', 4620);

-- ===== Арматура (цена за пог. метр) =====
INSERT INTO materials (name, unit, current_price) VALUES
                                                      ('Арматура 8 мм',  'п.м.', 23),
                                                      ('Арматура 14 мм', 'п.м.', 69);
-- Примечание: продаётся прутками 6м.
-- В FoundationCalculator рассчитывается количество прутков 6м для арматуры 14мм.
-- Арматура 8мм считается в пог. метрах.

-- V8: Создание первого пользователя-администратора (пароль: admin123 в bcrypt)
INSERT INTO users (login, password, first_name, last_name, role)
VALUES ('admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Администратор', 'Системы', 'ADMIN');