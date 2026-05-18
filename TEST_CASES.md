# HCBS Test Cases

| Test Case ID | Test Case Name | Purpose | Condition | Expected Result | Actual Result |
| --- | --- | --- | --- | --- | --- |
| TC_001 | Film listing displays showings | Verify listing view supports browsing films and sessions | Open Film Listing page | Showings are displayed with film, cinema, screen, date, time, and available seats | Covered by seeded data and UI |
| TC_002 | Filter showings | Verify users can narrow listings | Choose city, cinema, date, or film title | Grid updates to matching showings | Covered by FilmListingService |
| TC_003 | Successful booking | Verify a booking can be created | Select showing and available seats | Booking reference and receipt are generated | Covered by `createsBookingWithReceiptValuesAndReservesSeats` |
| TC_004 | Receipt details | Verify receipt contains required coursework fields | Create a booking | Receipt includes reference, film, date, time, screen, tickets, seats, total cost, booking date | Covered by BookingView |
| TC_005 | Duplicate seat prevention | Verify same seat cannot be booked twice for same showing | Book an already reserved seat | System rejects duplicate booking | Covered by `rejectsDuplicateSeatForSameShowing` |
| TC_006 | Price calculation | Verify price uses city, time band, and seat area | Book London evening lower hall seats | Price is £12 per lower hall seat for London evening | Covered by BookingService |
| TC_007 | Advance booking limit | Verify booking date rule | Try to book more than 7 days ahead | System rejects booking | Covered by BookingService validation |
| TC_008 | Successful cancellation | Verify valid cancellation | Cancel before show date | Booking status becomes CANCELLED | Covered by `cancelsBookingAndAppliesFiftyPercentCharge` |
| TC_009 | Cancellation charge | Verify 50% cancellation fee | Cancel a £12 booking | Cancellation charge is £6 | Covered by `cancelsBookingAndAppliesFiftyPercentCharge` |
| TC_010 | Same-day cancellation rejection | Verify no same-day cancellation | Cancel on show date | System rejects cancellation | Covered by CancellationService rule |
| TC_011 | Unknown booking reference | Verify lookup validation | Search invalid reference | System shows not found message | Covered by CancellationView and service exception |
