var UmpyrPages = [{
        id: 0,
        title: 'Home',
        navId: 'home-nav',
        activeCards: [11],
    },
    {
        id: 1,
        title: 'Matches',
        navId: 'matches-nav',
        activeCards: [13, 7],
    },
    {
        id: 2,
        title: 'Communications',
        navId: 'communications-nav',
        activeCards: [1, 6],
    },
    {
        id: 3,
        title: 'Connections',
        navId: 'connections-nav',
        activeCards: [2, 3, 4],
    },
    {
        id: 4,
        title: 'History',
        navId: 'history-nav',
        activeCards: [8],
    },
    {
        id: 5,
        title: 'New Score',
        navId: 'new-score-nav',
        activeCards: [0],
    },
    {
        id: 6,
        title: 'Friends',
        navId: 'friends-nav',
        activeCards: [2],
    },
    {
        id: 7,
        title: 'Admin',
        navId: 'admin-nav',
        activeCards: [15],
    },
    {
        id: 9,
        title: 'Help',
        navId: 'help-nav',
        activeCards: [14],
    },
];

var UmpyrCards = [{
        id: 0,
        title: 'Score Entry',
        chipId: null,
        htmlSource: '/components/cards/scoreentrycard.html',
        placeholderId: 'score-entry-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new ScoreEntryCardController(parentDiv); }
    },
    {
        id: 1,
        title: 'Messages',
        chipId: 'messages_toggle',
        htmlSource: '/components/cards/messagescard.html',
        placeholderId: 'messages-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new MessagesCardController(parentDiv); }
    },
    {
        id: 2,
        title: 'Friends',
        chipId: 'friends_toggle',
        htmlSource: '/components/cards/friendscard.html',
        placeholderId: 'friends-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new FriendsCardController(parentDiv); }
    },
    {
        id: 3,
        title: 'Groups',
        chipId: 'group_toggle',
        htmlSource: '/components/cards/groupscard.html',
        placeholderId: 'groups-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new GroupsCardController(parentDiv); }
    },
    {
        id: 4,
        title: 'Locations',
        chipId: 'locations_toggle',
        htmlSource: '/components/cards/locationscard.html',
        placeholderId: 'locations-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new LocationsCardController(parentDiv); }
    },
    {
        id: 5,
        title: 'Updates',
        chipId: 'updates_toggle',
        htmlSource: '/components/cards/updatescard.html',
        placeholderId: 'updates-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new UpdatesCardController(parentDiv); }
    },
    {
        id: 6,
        title: 'Posts',
        chipId: 'posts_toggle',
        htmlSource: '/components/cards/postscard.html',
        placeholderId: 'posts-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new PostsCardController(parentDiv); }
    },
    {
        id: 7,
        title: 'Matches',
        chipId: 'matches_toggle',
        htmlSource: '/components/cards/matchescard.html',
        placeholderId: 'matches-display-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new MatchesCardController(parentDiv, false); }
    },
    {
        id: 8,
        title: 'History',
        chipId: 'history_toggle',
        htmlSource: '/components/cards/historycard.html',
        placeholderId: 'history-display-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new HistoryCardController(parentDiv); }
    },
    {
        id: 9,
        title: 'Welcome',
        chipId: null,
        htmlSource: '/components/cards/welcomecard.html',
        placeholderId: 'welcome-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new WelcomeCardController(parentDiv); }
    },
    {
        id: 10,
        title: 'Match Details',
        chipId: null,
        htmlSource: '/components/cards/matchdetailscard.html',
        placeholderId: 'match-details-display-card',
        closeButtonId: 'match-details-card-close',
        controller: null,
        createController: function(parentDiv) { return new MatchDetailsCardController(parentDiv); }
    },
    {
        id: 11,
        title: 'User Details',
        chipId: null,
        htmlSource: '/components/cards/userdetailscard.html',
        placeholderId: 'user-details-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new UserDetailsCardController(parentDiv); }
    },
    {
        id: 12,
        title: 'Find Friends',
        chipId: 'friends_find_toggle',
        htmlSource: '/components/cards/friendsfindcard.html',
        placeholderId: 'friends-find-card',
        closeButtonId: 'friends-find-card-close',
        controller: null,
        createController: function(parentDiv) { return new FriendsFindCardController(parentDiv); }
    },
    {
        id: 13,
        title: 'Matches Submitted',
        chipId: 'matches_submitted_toggle',
        htmlSource: '/components/cards/matchescard.html',
        placeholderId: 'matches-submitted-display-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new MatchesCardController(parentDiv, true); }
    },
    {
        id: 14,
        title: 'Help',
        chipId: 'help_toggle',
        htmlSource: '/components/cards/helpcard.html',
        placeholderId: 'help-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new HelpCardController(parentDiv); }
    },
    {
        id: 15,
        title: 'Site Messages',
        chipId: 'site_messages_toggle',
        htmlSource: '/components/cards/messagescard.html',
        placeholderId: 'site-messages-card',
        closeButtonId: null,
        controller: null,
        createController: function(parentDiv) { return new MessagesCardController(parentDiv, true); }
    },

];