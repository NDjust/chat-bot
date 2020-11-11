-- create table
CREATE TABLE dc_data (
    id bigint auto_increment primary key,
    title VARCHAR(1000),
    view_count int,
    recommend_count int
)ENGINE=InnoDB CHARACTER SET=utf8;