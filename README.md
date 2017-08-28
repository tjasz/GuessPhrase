# GuessPhrase
GuessPhrase is an Android application for a phrase-guessing game similar to Hasbro's Catchprhase.
One notable difference, besides the mobile interface, from Hasbro's game is that
GuessPhrase allows the user to add custom categories.
The primary user interface components are described below.

## Download and Install

GuessPhrase can be downloaded and installed from the [Google Play Store](https://play.google.com/store/apps/details?id=com.hotmail.nukorausa.guessphrase).

## Usage

### Main Menu
When the application loads, a main menu is shown, giving the user two options:
to start a new game, or to resume a previously started one (if a saved game exists).
Whenever gameplay is paused, the current game is written to the save file.
In this way, if a call or other activity interrupts GuessPhrase,
users can always get back to the game that was in progress at the time.

Main Menu Screen
<img src="https://github.com/tjasz/GuessPhrase/blob/master/pic/screenshot_main_menu.png?raw=true" alt="Main Menu Screen" style="width: 200px;">

### Category Selection
When the user opts to play a new game, they are asked to select a category to play.
The list includes the default categories that ship with GuessPhrase
as well as any custom categories previously created by the user.
A user may also choose to create a custom category,
a process described in greater detail below.
Once a category is selected, gameplay begins.

Select Category Screen
<img src="https://github.com/tjasz/GuessPhrase/blob/master/pic/screenshot_select_category.png?raw=true" alt="Select Category Screen" style="width: 200px;">

### Creating a Custom Category
Custom categories in GuessPhrase are based off of the page links in Wikipedia pages.
Ideally, the set of Wiki pages linked to from another page form a collection of related words and phrases.
This idea is the basis for creation of categories.
To create a new category, the user is asked for a list of one or more Wikipedia page titles
and a category name.
The titles of each page linked to by the provided pages form the items in the new category.

Add Category Screen:
<img src="https://github.com/tjasz/GuessPhrase/blob/master/pic/screenshot_add_category.png?raw=true" alt="Add Category Screen" style="width: 200px;">

### Gameplay
Like Hasbro's Catchphrase, GuessPhrase is a team-based phrase-guessing game.
An even number of players split into two teams.
The players arrange themselves in a circle such that no one is sitting/standing adjacent to any of their teammates.
In other words, every other player is a member of the same team.

When gameplay starts, an item from the category is displayed and a countdown timer begins.
The person currently holding the device must describe this phrase to their team,
without using the phrase itself or any words/syllables that make it up.
A phrase may be skipped simply by tapping it to load a new one.
Once the team of the current player says the phrase, s/he passes the device to the next person
(who is on the other team)

Game Screen:
<img src="https://github.com/tjasz/GuessPhrase/blob/master/pic/screenshot_gameplay.png?raw=true" alt="Game Screen" style="width: 200px;">

Game play continues in this manner until the countdown timer stops.
At this point, the player who was holding the device when the countdown stopped must give the opposing team a point.
Then gameplay resumes as described above.
The game ends when a team reaches 7 points and is declared the winner.

Dialog Asking for a Round Winner:
<img src="https://github.com/tjasz/GuessPhrase/blob/master/pic/screenshot_get_winner.png?raw=true" alt="Dialog Asking for a Round Winner" style="width: 200px;">
