# Spotify clone

#### Copyright 2023 - Dumitrescu Toma-Ioan


## Description
The project implements spotify core functionalities. The current stage
of project focuses on performing statistics on user listening activity,
making song recommendations, monetization for artist products in function
of the user type, more complex page navigation, running ads, notification
systems for subscribers. The testing part is only for the current stage,
since the core functionalities (audio player, pagination, search bar,
notification manager, user types) were already tested (stages 1 and 2)
after implementation.

## Implementation details
For performing different statistics for a specific user, the listens will be
recorded in a data structure, in the player class. Because the statistics
implied only printing the top from a list of recordings, the hashmap is the
most efficient way to count audio files apparitions. For a particular user
player, recordedEntries stores every library entry that was loaded, excepting
ads. Evidently, using an arraylist will be more flexible for more statistics,
but far less efficient. Statistics for content creators will be done by
iterating over all users recorded entries. For less cognitive complexity, in
wrap functions, the recordedEntries will be split in linked hashmaps (for
sorting possibility) with more concrete types (Song, Episode, Album ...).
After sorting decreasingly by listens, only top 5 or less will be displayed
using the object mapper. This principle is applied for content creator wrap.

Storing the recorded entries is done by catching the products from the
player source in setSource() (first audio file if it is a collection) and
in next (if there is a collection). In recordedEntries, both collections
and audio files are stored. The ads are not loaded, just recorded after
the current audio files finishes, minding that if another load overwrites
the waiting ad, the ad field will be initialized to null, so it can't be
recorded. For simplicity, a new arraylist for recorded songs and ads will
be used.

Monetization will be tracked only at the end of the program, because there
are many cases in which live credit transfer is not possible. Only artists
with merch revenue and listens will be taken into consideration for display.
Computing the revenue will be performed in two steps, firstly for user premium
listening, and then for ads (free subscription to spotify). The functionality
is not completed, since it does not work for complex cases. After calculating
the revenue for each song by creating a set of songs from all recorded songs,
for every artist, the most profitable song is determined using the algorithm
for finding the maximum in an array. In equality cases, the lexicographical
order is taken into consideration, verifying that the ref max is not 0.
Buying a merch will be done using a user pay method that will transfer the
money in artist revenue account and store the name in a dedicated user list.

The premium subscription will be tracked by registering in the copy song
that it was listened in this regime. Later, when calculating the revenues,
the premium boolean field will tell which formula to use.

The notification manager will hold a list with observers (users). After
performing a certain action (add an album, event, announcement), that method 
will inform notification manager to notify all observers. To verify if a user
should be notified (add a notification in user news list), the content
creator's name should be on the user list of subscriptions.

Since all recommendations appear on HomePage, the user will retain a history of
recommendations that can be later used in printing the page. The remained
duration of the current song will be the seed for generating the index of the
recommendation, with the upper bound specificSongs.size(), where specificSongs
are from the targeted genre. The top five fans will be extracted from the
users listens, by decreasingly sorting the number of listens of the user's
current song artist and taking top 5. From all fans, top 5 songs will be
extracted and added to the recommended playlist list of songs, eliminating
duplicates. For the simple recommendation of playlist, the genres are collected
from the mentioned lists in a hashmap to efficiently handle duplicates
(getOrDefault hashmap method), counts and element access. Using a linked hashmap
is needed for sorting the genres hashmap that will lead to making the playlist
list of songs from top 5 songs of first genre, 3 for second and two for third.

Two fields will retain last recommended song and playlist (one will equal null),
facilitating the load (just setting the source and then resuming the player).

For page navigation, the page history is an arraylist. The current page is
described by a current index, and nextPage, prevPage mean incrementing or
decrementing the index if possible (if the page exists). History additions
are done in Admin changePage method, including the requested forward reset.

## Design patterns
Factory -> UserAbstract, used for obj creation encapsulation, it instantiates
a user, artist or host, by passing the user type, username, age, city as param.

Singleton, lazy instantiation -> UserWrap, ArtistWrap, HostWrap, Admin, used
because there is need only for one (global) instance of the mentioned classes.

Observer -> Subject = Notification Manager, Observer = interface (...user);
notifies users for new events from their subscriptions. Used for flexibility
(new features like artists, hosts notified of platform events), live updates.

Strategy -> Strategy = WrapStrategy, ConcreteStrategies = UserWrap, ArtistWrap,
HostWrap, Context = Admin (not interacting with concrete strategies),
CommandRunner instantiates the concrete strategy based on the command input.
Used for statistics extensibility, algorithms encapsulation.

## Bibliography
https://docs.oracle.com/en/java/javase/19/
