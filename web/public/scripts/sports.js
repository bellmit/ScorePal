function SportFromMode(scoreMode) {
    for (var i = 0; i < UmpyrSports.length; i += 1) {
        if (Number(UmpyrSports[i].id) == Number(scoreMode)) {
            // this is the matching id to mode, this is the one to return
            return UmpyrSports[i];
        }
    }
    // if here, there was no match
    console.error('There is no sport for the specified mode of ' + scoreMode);
    return undefined;
}

function SportTitleIsValid(sportTitle) {
    // return if the passed title is in the list of sports
    for (var i = 0; i < UmpyrSports.length; i += 1) {
        if (UmpyrSports[i].title === sportTitle) {
            return true;
        }
    }
    // if here then the title is not valid
    return false;
}

function SportFromTitle(sportTitle) {
    for (var i = 0; i < UmpyrSports.length; i += 1) {
        if (UmpyrSports[i].title === sportTitle) {
            // this is the matching title, this is the one to return
            return UmpyrSports[i];
        }
    }
    // if here, there was no match
    console.error('There is no sport for the specified title of ' + sportTitle);
    return undefined;
}

var UmpyrSports = [{
        id: 0,
        title: 'Points',
        controller: null,
        setsOptions: [],
        createController: function(setsOption) { return new ScorePoints(0, null, null, null); },
        imageStyle: 'card-image-points',
        imageRef: 'https://www.flickr.com/photos/speedcandy/426962381/in/photolist-DJi3n-25fne-5YzuaA-8KA6uK-e9Vx2p-ebiH4u-c6RS9m-oBEuei-djmRqv-9vYVXz-d7Nf2A-2AQqz-5Yzv7m-7ngLGY-7ty53T-2EeYeB-5BpAm-62Yv2h-c6RQ1h-c6RPJQ-c6RRW7-4mBVnT-edgVf9-qDXCk8-9w2Xn9-c6RQPJ-c6Ryds-c6RVp1-qWod2a-48CwwJ-H65U47-cQvsiG-4nJAa9-b9NUZH-7qfDyG-3D8xVh-8KDdUW-8vQiX2-9z3V4n-dsFPTb-bbveeZ-a8Gxm8-e9VwzX-e4mG7-mCUkH1-5ww2zH-c6RSkq-J4CqWJ-5ww1yt-4BCAsJ',
        imageAttrib: "&quot;On the Board&quot; (CC BY 2.0) by bobbychuck24",
        attribImgClass: "ccoverlaywhite",
        scoringTitles: ['Point', '', 'Point'],
    },
    {
        id: 1,
        title: 'Tennis',
        controller: null,
        setsOptions: [1, 3, 5],
        createController: function(setsOption) { return new ScoreTennis(setsOption); },
        imageStyle: 'card-image-tennis',
        imageRef: 'https://www.flickr.com/photos/krbo_sb/33309416641/in/photolist-SKrt5V-iKcgL9-TCHhUQ-22Tj7F-phJmt-5ukzDv-TL7xp2-V3SuTa-qYqje5-dMSaNn-54Sj9U-aAM8ZJ-acSs9R-aAM8Qy-ccNHb1-dStWUo-DF4Zbr-9DiXYG-5uQM7U-93RUp7-7HYdmW-6MQxFj-VYEpoU-Tc3gbq-4Lbxc6-ocAvhG-pPRvZC-bXPwjh-WD8yvq-aAM97Y-3GpfJ-9cChrd-WGqGiX-cy4yMm-aNBNu-6rgCFX-7GP2Tu-8pCLjG-9LhDjG-61SMVA-Va1uFT-5B9UFo-oSVBSo-fTZrBz-7mMRz3-UKVgXe-vgMbDo-bA2jbR-TFjLEZ-nhbwwB',
        imageAttrib: "&quot;Waiting...&quot; (CC BY 2.0) by krbo_sb",
        attribImgClass: "ccoverlaywhite",
        scoringTitles: ['Set', 'Game', 'Point'],
    },
    {
        id: 2,
        title: 'Badminton',
        controller: null,
        setsOptions: [1, 3, 5],
        createController: function(setsOption) { return new ScorePoints(setsOption, 21, 2, 30); },
        imageStyle: 'card-image-badminton',
        imageRef: 'https://www.flickr.com/photos/sanmitrakale/33711328291/in/photolist-TmXnq8-bZjGj7-5nyXaq-uZV6Q-acxSRq-SLns6N-M1HCr-2Tr3Uk-PPHRyk-bdxnCt-dYw3fw-49UWLz-6Y3ake-9x5NjP-6G66VL-6FQta6-5qjHEM-wcsmyU-kddYLw-9UNGqv-M1z6W-eG9P2P-bdxnnt-aqfErC-5MjGAh-hj2vds-BcVhKn-8KMnax-qiWThK-cJYkUj-5MjHoh-vhogY-93v2YH-PHzYV-7ExS6G-7Eu1ec-7Eu1Fn-6JpfLV-cK1oVE-bizKrp-aiYpF-jDbYJ-cJYsh7-cJZ6mj-8sRrsy-sfGi2y-aFPmN-sxguT2-cco9yA-dc27wa',
        imageAttrib: "&quot;The sport of badminton&quot; (CC BY-SA 2.0) by solarisgirl",
        attribImgClass: "ccoverlay",
        scoringTitles: ['Game', 'Result', 'Point'],
    },
];