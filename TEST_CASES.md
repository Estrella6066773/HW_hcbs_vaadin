# HCBS Test Cases

| Test Case ID | Test Case Name | Purpose | Condition | Expected Result | Actual Result |
| --- | --- | --- | --- | --- | --- |
| TC_001 | Film listing displays showings | Verify listing view supports browsing films and sessions | Open Film Listing page | Showings displayed with film, description, actors, cinema, screen, date, time, available seats | `FilmListingServiceTest` + UI |
| TC_002 | Filter showings | Verify users can narrow listings | Choose city, cinema, date, or film title | Grid updates via `ShowingRepository.searchShowings` | `FilmListingServiceTest` |
| TC_003 | Successful booking | Verify a booking can be created | Select showing and available seats | `BookingReceipt` with reference generated | `BookingServiceTest.createsBookingWithReceiptValuesAndReservesSeats` |
| TC_004 | Receipt details | Verify receipt contains required coursework fields | Create a booking | All receipt fields in `BookingReceipt.toReceiptText()` | `BookingView` + service test |
| TC_005 | Duplicate seat prevention | Verify same seat cannot be booked twice | Book an already reserved seat | Rejected; active reservation query only counts CONFIRMED | `rejectsDuplicateSeatForSameShowing` |
| TC_006 | Price calculation | Verify price uses city, time band, and seat area | Book London evening lower hall seats | £12 per seat (2 seats = £24) | `BookingServiceTest` total £24.00 |
| TC_007 | Advance booking limit | Verify booking date rule | Book showing more than 7 days ahead | Rejected with one-week message | `rejectsBookingMoreThanSevenDaysInAdvance` |
| TC_008 | Successful cancellation | Verify valid cancellation | Cancel before show date | CANCELLED; seats released | `cancelsBookingAndAppliesFiftyPercentCharge` |
| TC_009 | Cancellation charge | Verify 50% cancellation fee | Cancel a £12 booking | Charge is £6 | `cancelsBookingAndAppliesFiftyPercentCharge` |
| TC_010 | Same-day cancellation rejection | Verify no same-day cancellation | Cancel on show date | Rejected | `rejectsSameDayCancellation` |
| TC_011 | Unknown booking reference | Verify lookup validation | Search invalid reference | `IllegalArgumentException` message in UI notification | Manual / UI test |
