# 成员 A — 影片浏览 · 观看顺序

> 从上到下依次点开阅读。全量文件索引：[四人分工.md §成员 A](../四人分工.md#成员-a--模块-1影片浏览home)

---

## 0 · 跑应用

```powershell
./mvnw spring-boot:run
```

浏览器：`/` → 筛选 Search → `/film/{id}` → Book（见 B 模块）

---

## 1 · 业务地图

| # | 阅读 |
|:-:|------|
| 1 | [TEST_CASES.md](../TEST_CASES.md)（TC_001、TC_002） |
| 2 | [home/README.md](../../src/main/java/com/hcbs/web/home/README.md) |
| 3 | [home/component/README.md](../../src/main/java/com/hcbs/web/home/component/README.md) |

---

## 2 · 测试

| # | 阅读 |
|:-:|------|
| 4 | [FilmListingServiceTest.java](../../src/test/java/com/hcbs/service/listing/FilmListingServiceTest.java) |
| 5 | [HcbsSearchServiceTest.java](../../src/test/java/com/hcbs/service/search/HcbsSearchServiceTest.java) |
| 6 | [FilmCatalogServiceTest.java](../../src/test/java/com/hcbs/service/catalog/FilmCatalogServiceTest.java) |

```powershell
./mvnw test -Dtest=HcbsSearchServiceTest,FilmListingServiceTest,FilmCatalogServiceTest
```

---

## 3 · Model

| # | 阅读 |
|:-:|------|
| 7 | [City.java](../../src/main/java/com/hcbs/model/City.java) |
| 8 | [Cinema.java](../../src/main/java/com/hcbs/model/Cinema.java) |
| 9 | [Screen.java](../../src/main/java/com/hcbs/model/Screen.java) |
| 10 | [Film.java](../../src/main/java/com/hcbs/model/Film.java) |
| 11 | [Actor.java](../../src/main/java/com/hcbs/model/Actor.java) |
| 12 | [FilmActor.java](../../src/main/java/com/hcbs/model/FilmActor.java) |
| 13 | [Showing.java](../../src/main/java/com/hcbs/model/Showing.java) |
| 14 | [ShowingStatus.java](../../src/main/java/com/hcbs/model/ShowingStatus.java) |
| 15 | [PriceRule.java](../../src/main/java/com/hcbs/model/PriceRule.java) |
| 16 | [TimeBand.java](../../src/main/java/com/hcbs/model/TimeBand.java) |

---

## 4 · DTO

| # | 阅读 |
|:-:|------|
| 17 | [ShowingListingFilter.java](../../src/main/java/com/hcbs/dto/ShowingListingFilter.java) |
| 18 | [FilmCatalogFilter.java](../../src/main/java/com/hcbs/dto/FilmCatalogFilter.java) |
| 19 | [ShowingRow.java](../../src/main/java/com/hcbs/dto/ShowingRow.java) |
| 20 | [ShowingListingResult.java](../../src/main/java/com/hcbs/dto/ShowingListingResult.java) |
| 21 | [FilmCardDto.java](../../src/main/java/com/hcbs/dto/FilmCardDto.java) |
| 22 | [FilmDetailDto.java](../../src/main/java/com/hcbs/dto/FilmDetailDto.java) |
| 23 | [CityOption.java](../../src/main/java/com/hcbs/dto/CityOption.java) |
| 24 | [CinemaOption.java](../../src/main/java/com/hcbs/dto/CinemaOption.java) |

---

## 5 · Service

| # | 阅读 |
|:-:|------|
| 25 | [FilmListingService.java](../../src/main/java/com/hcbs/service/listing/FilmListingService.java) |
| 26 | [FilmCatalogService.java](../../src/main/java/com/hcbs/service/catalog/FilmCatalogService.java) |
| 27 | [HcbsSearchService.java](../../src/main/java/com/hcbs/service/search/HcbsSearchService.java) |
| 28 | [PosterResourceService.java](../../src/main/java/com/hcbs/service/catalog/PosterResourceService.java) |

---

## 6 · Repository

| # | 阅读 |
|:-:|------|
| 29 | [ShowingRepository.java](../../src/main/java/com/hcbs/repository/ShowingRepository.java) |
| 30 | [FilmRepository.java](../../src/main/java/com/hcbs/repository/FilmRepository.java) |
| 31 | [CityRepository.java](../../src/main/java/com/hcbs/repository/CityRepository.java) |
| 32 | [CinemaRepository.java](../../src/main/java/com/hcbs/repository/CinemaRepository.java) |
| 33 | [ScreenRepository.java](../../src/main/java/com/hcbs/repository/ScreenRepository.java) |
| 34 | [FilmActorRepository.java](../../src/main/java/com/hcbs/repository/FilmActorRepository.java) |
| 35 | [ActorRepository.java](../../src/main/java/com/hcbs/repository/ActorRepository.java) |
| 36 | [PriceRuleRepository.java](../../src/main/java/com/hcbs/repository/PriceRuleRepository.java) |

---

## 7 · Web

| # | 阅读 |
|:-:|------|
| 37 | [ShowingFilterQuery.java](../../src/main/java/com/hcbs/web/home/ShowingFilterQuery.java) |
| 38 | [FilmRecommendView.java](../../src/main/java/com/hcbs/web/home/FilmRecommendView.java) |
| 39 | [FilmDetailView.java](../../src/main/java/com/hcbs/web/home/FilmDetailView.java) |
| 40 | [AdditiveShowingFilterPanel.java](../../src/main/java/com/hcbs/web/home/component/AdditiveShowingFilterPanel.java) |
| 41 | [FilmPoster.java](../../src/main/java/com/hcbs/web/home/component/FilmPoster.java) |
| 42 | [BackToHomeAction.java](../../src/main/java/com/hcbs/web/home/component/BackToHomeAction.java) |

---

## 8 · 配置与资源

| # | 阅读 |
|:-:|------|
| 43 | [FilmPosterCatalog.java](../../src/main/java/com/hcbs/config/FilmPosterCatalog.java) |
| 44 | [HcbsMediaCatalog.java](../../src/main/java/com/hcbs/config/HcbsMediaCatalog.java) |
| 45 | [images/posters/](../../src/main/resources/META-INF/resources/images/posters/) |
| 46 | [images/banners/](../../src/main/resources/META-INF/resources/images/banners/) |

---

## 9 · 交界（只读）

| # | 阅读 |
|:-:|------|
| 47 | [BookingView.java](../../src/main/java/com/hcbs/web/booking/BookingView.java)（`showingId` 入参） |
| 48 | [MainLayout.java](../../src/main/java/com/hcbs/web/shell/MainLayout.java)（Home 菜单） |
