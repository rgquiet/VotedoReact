// Test data: A lot of users already in a test session with the same track

db = db.getSiblingDB("votedo");
db.dropDatabase();

db.users.insertMany([
    {
        _id: "vqyrcsnd84efzir5ukl2u5jy7",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "rg_quiet",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "rg_quiet",
        email: "robinguedel@gmail.com",
        imgUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "12346",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "12347",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "12348",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "12349",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123410",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123411",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123412",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123413",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123414",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123415",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123416",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123417",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123418",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123419",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123420",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123421",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123422",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123423",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123424",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123425",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123426",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "123427",
        sessionId: "5eec9863b6c08928fd7435a7",
        trackId: "5UWwZ5lm5PKu6eKsHAGxOk",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },
]);

db.sessions.insertOne({
    _id: ObjectId("5eec9863b6c08928fd7435a7"),
    name: "Test",
    open: true,
    owner: {$ref: "users", $id: "vqyrcsnd84efzir5ukl2u5jy7"},
    members: [
        {$ref: "users", $id: "rg_quiet"},
        {$ref: "users", $id: "12346"},
        {$ref: "users", $id: "12347"},
        {$ref: "users", $id: "12348"},
        {$ref: "users", $id: "12349"},
        {$ref: "users", $id: "123410"},
        {$ref: "users", $id: "123411"},
        {$ref: "users", $id: "123412"},
        {$ref: "users", $id: "123413"},
        {$ref: "users", $id: "123414"},
        {$ref: "users", $id: "123415"},
        {$ref: "users", $id: "123416"},
        {$ref: "users", $id: "123417"},
        {$ref: "users", $id: "123418"},
        {$ref: "users", $id: "123419"},
        {$ref: "users", $id: "123420"},
        {$ref: "users", $id: "123421"},
        {$ref: "users", $id: "123422"},
        {$ref: "users", $id: "123423"},
        {$ref: "users", $id: "123424"},
        {$ref: "users", $id: "123425"},
        {$ref: "users", $id: "123426"},
        {$ref: "users", $id: "123427"}
    ],
    currentTrack: {
        trackInfos: {
            _id: "1igUMb7IYNpPtAPGgDX6Hi",
            name: "Twisted Fate",
            artist: "The Basterds",
            imgUrl: "https://i.scdn.co/image/ab67616d0000b273fef6632b70a18260c6b70ab6",
            timeMs: 217153
        },
        progressMs: 0,
        timestamp: 1596698049448
    },
    votes: [],
    _class: "com.rgq.votedoreact.model.Session"
});

db.createCollection("vqyrcsnd84efzir5ukl2u5jy7", { capped: true, size: 4, max: 10 });
db.createCollection("rg_quiet", { capped: true, size: 4, max: 10 });
