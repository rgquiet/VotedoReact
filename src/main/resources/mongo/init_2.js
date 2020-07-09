// Test data: Only users without a session

db = db.getSiblingDB("votedo");
db.dropDatabase();

db.users.insertMany([
    {
        _id: "vqyrcsnd84efzir5ukl2u5jy7",
        username: "RobWob",
        email: "funadresse@gmx.ch",
        imgUrl: "https://i.scdn.co/image/ab6775700000ee8558dec5dc13a2ca415586179f",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    },{
        _id: "rg_quiet",
        username: "rg_quiet",
        email: "robinguedel@gmail.com",
        imgUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Nicolas_Cage_Deauville_2013.jpg/330px-Nicolas_Cage_Deauville_2013.jpg",
        friends: [],
        votes: 0,
        _class: "com.rgq.votedoreact.model.User"
    }
]);

db.createCollection("vqyrcsnd84efzir5ukl2u5jy7", { capped: true, size: 4, max: 10 });
db.createCollection("rg_quiet", { capped: true, size: 4, max: 10 });
