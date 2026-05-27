# Closed Testing Feedback

## Day 1

### Albait Developments

Rate feedback quality:
★
★
★
★
★

Login with google account: Works great! It saved my movies after reinstallation

Search Movies: Works correctly, I can find any movie. The interface looks good.

Navigate to movie detail and mark movies as watched: The detail has all the information I need. Maybe you could add the trailer?

Navigate to movie detail and add movies to watchlist: Works perfectly, then I can access my list and mark it as "watched"

### Bruno Manchinelli

Rate feedback quality:
★
★
★
★
★

Login with google account: Good handling of states. Appreciate the guest login feature.

Search Movies: The initial state for the empty result before showing the loading spinner is a little confusing. Could be improved.

Navigate to movie detail and mark movies as watched: Working good. The watched movies screen doesn't need loading, meaning that the app has good architecture and uses the repository pattern.

Navigate to movie detail and add movies to watchlist: Working good. Changing a movie from watchlist to watched is fast and each screen is automatically updated.

### Julian Ybarra

Rate feedback quality:
★
★
★
★
★

Login with google account: Error cases are displayed well, and Google login works. It saved my data because I had entered as a guest, and when I logged in again with my Google account, it recovered what I had previously saved.

Search Movies: The search works well, the category chips too; maybe being able to search more than one category.

Navigate to movie detail and mark movies as watched: The navigation to the movie detail is good, I like how the top app bar darkens when you scroll.

Navigate to movie detail and add movies to watchlist: The navigation to the movie detail is good. At some moments I did not remember whether it was watched or in the list, but I think that is due to limited use.

### Unknown Tester

Login with google account: Login as guest. Was able to switch between views and tabs. No problems

Search Movies: The search movie works well, no problem with that.

Navigate to movie detail and mark movies as watched: The two tabs update well when checking a movie as Watched or in Watchlist.

Navigate to movie detail and add movies to watchlist: Some behavior to report during testing: - swipe to delete is a bit weird to have the icon at the center. Better to have it when starting to drag, at the start or end - Welcome Santoro bottom sheet is not dismissible with gesture

### Rohit Ojha

Rate feedback quality:
★
★
★
★
★

Login with google account: Google login worked smoothly during testing. The authentication process was quick, secure, and easy to complete without any errors or unnecessary steps. Account access was granted instantly after login.

Search Movies: The movie search feature performed really well. Search results appeared quickly and the recommendations were accurate based on the entered keywords. The interface was clean and easy to use.

Navigate to movie detail and mark movies as watched: I was able to navigate to the movie detail page without any issues. Marking movies as watched worked correctly and the status updated instantly in the app, making the experience smooth and intuitive.

Navigate to movie detail and add movies to watchlist: The watchlist feature worked perfectly during testing. Adding movies from the detail page was simple and the selected movies appeared correctly in the watchlist section without any delay or syncing issues.

### Surya Darma

Rate feedback quality:
★
★
★
★
★

Login with google account: Google Sign-In worked seamlessly. Account picker appeared immediately showing the saved account. Two-step confirmation clearly explains what data is shared with the app. Login completed instantly with no errors and landed directly on the Search screen.

Search Movies: Search is fast and responsive. Typing 'Inception' returned multiple relevant results immediately, displayed in a clean grid with movie posters. The correct film appeared as the first result. Placeholder icons show neatly when a poster is unavailable.

Navigate to movie detail and mark movies as watched: Movie detail page is well-organized: poster, title, tagline, star rating, genre chips, year, country, runtime, overview, cast with photos, and crew. Tapping 'Watched' instantly toggles the button to a filled dark state with a checkmark icon. State change is immediate and visually clear.

Navigate to movie detail and add movies to watchlist: The Watchlist button toggles to 'In Watchlist' with a bold black filled style and bookmark icon. Both Watched and Watchlist states can be active simultaneously, which is the correct expected behavior. Visual feedback is immediate and the two states are clearly differentiated.

### Bernardo Belchior

Rate feedback quality:
★
★
★
★
★

Login with google account: The text in the "Login with Google" isn't visible in dark mode. It's likely white text on white background.

Search Movies: Searching for movies works. When I opened a movie there was a tooltip with a white background that probably had white text, so I can't read it.

Navigate to movie detail and mark movies as watched: It worked. When I search the movie again, it would be nice to have some kind of ribbon to show that I've already watched that movie.

Navigate to movie detail and add movies to watchlist: Done. Similar to marking as watched, it would probably be nice to have a ribbon indicating that a movie in the search results page is in the watchlist.

## Day 4

### Julian Ybarra

Rate feedback quality:
★
★
★
★
★

Login with google account: I logged out and logged in again with Google, and it works well. The only thing is that the watched and saved movies were deleted; I do not remember deleting the account data.

Search Movies: The search works well. As I said about the categories, maybe allow more than one.

Navigate to movie detail and mark movies as watched: Navigating to the detail and marking it as watched works well. I like the toolbar animation.

Navigate to movie detail and add movies to watchlist: Navigating to the detail and marking it to watch works well.

### Bernardo Belchior

Rate feedback quality:
★
★
★
★
★

Login with google account: Sorry, not comfortable with this. I can test other features, though.

Search Movies: Search works well. It searches both my localized language and the movie's original title. Good job there.

Navigate to movie detail and mark movies as watched: Same feedback as I gave last time. It works, but the UI could be slightly polished.

Navigate to movie detail and add movies to watchlist: Same feedback as I gave last time. It works, but the UI could be slightly polished.

## Future Improvements

### Local Data Per User

Persist local movie state per user ID in Room so watched movies and watchlist entries are isolated between accounts on the same device. This would allow logout to avoid clearing local data while still preventing another account from seeing the previous user's local state.
