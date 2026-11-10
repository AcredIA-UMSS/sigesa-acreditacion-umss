-- V28: noticias y destacados (FSD-UC-035 / FSD-BR-10).
-- Sin adjuntos ni observaciones internas. featured se garantiza en aplicación (a lo sumo una publicada).

CREATE TABLE IF NOT EXISTS news_article (
    id              UUID PRIMARY KEY,
    title           VARCHAR(255)  NOT NULL,
    slug            VARCHAR(280)  NOT NULL,
    category        VARCHAR(32)   NOT NULL,
    summary         VARCHAR(500)  NOT NULL,
    body            TEXT          NOT NULL,
    status          VARCHAR(16)   NOT NULL,
    published_at    TIMESTAMP,
    featured        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    CONSTRAINT uk_news_article_slug UNIQUE (slug)
);

CREATE INDEX IF NOT EXISTS idx_news_article_status_published
    ON news_article (status, published_at);

CREATE INDEX IF NOT EXISTS idx_news_article_featured
    ON news_article (featured);
