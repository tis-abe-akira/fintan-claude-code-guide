-- カテゴリーマスターテーブル作成
CREATE TABLE category_master (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT uk_category_name UNIQUE (category_name)
);

-- カテゴリーマスターの初期データ投入
INSERT INTO category_master (category_name) VALUES
    ('家電'),
    ('家具'),
    ('文房具'),
    ('食品'),
    ('衣料品'),
    ('スポーツ用品'),
    ('書籍'),
    ('玩具'),
    ('日用品'),
    ('電化製品');

-- 商品マスターテーブル作成
CREATE TABLE product_master (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    model_number VARCHAR(50) NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    description VARCHAR(256) NOT NULL,
    photo VARCHAR(50) NOT NULL,
    price INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 制約
    CONSTRAINT chk_price_range CHECK (price >= 1 AND price <= 100000000),
    CONSTRAINT chk_date_order CHECK (start_date <= end_date),
    CONSTRAINT uk_model_start_date UNIQUE (model_number, start_date),

    -- 外部キー（カテゴリーマスター参照）
    CONSTRAINT fk_category FOREIGN KEY (category_name)
        REFERENCES category_master(category_name)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- インデックス作成（検索性能向上）
CREATE INDEX idx_product_category ON product_master(category_name);
CREATE INDEX idx_product_manufacturer ON product_master(manufacturer);
CREATE INDEX idx_product_date_range ON product_master(start_date, end_date);
