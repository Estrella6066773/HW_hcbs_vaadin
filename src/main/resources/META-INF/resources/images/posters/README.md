# 电影封面图片

每部影片在数据库 `film.poster_url` 中保存**本地路径**（例如 `/images/posters/spirited-away.jpg`）。

## 添加或更换封面

1. 将 JPEG 文件放入本目录（建议比例约 2:3，例如 400×600）。
2. 在管理页或种子数据里把 `poster_url` 设为 `/images/posters/你的文件名.jpg`。
3. 重启应用；若 H2 里仍是旧路径，可删 `./data/hcbs.mv.db` 后重新灌库。

## 缺失显示

若数据库有路径但本目录**没有对应文件**，界面会显示 **「缺失」**（演示：`solitude.jpg` 默认未附带文件）。

不要使用 SVG 几何占位图；仅使用真实图片文件。
