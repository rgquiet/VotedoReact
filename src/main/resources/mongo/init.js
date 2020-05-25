db = db.getSiblingDB("votedo");
db.dropDatabase();

db.users.insertMany([
    {
        _id: "testuser1",
        sessionId: "testsession1",
        username: "Pierce Hawthrone",
        email: "pierc@test.com",
        imgUrl: "https://vignette.wikia.nocookie.net/community-sitcom/images/6/67/Evil_Pierce.jpg/revision/latest?cb=20131121155926"
    },{
        _id: "testuser2",
        sessionId: "testsession1",
        username: "Jeff Winger",
        email: "jeff@test.com",
        imgUrl: "https://pbs.twimg.com/profile_images/898704711725064194/22M0y-WR_400x400.jpg"
    }
]);

db.createCollection("sessions", { capped: true, size: 5000000, max: 10000 });
db.sessions.insertOne({
    _id: "testsession1",
    name: "This is a test",
    open: true,
    owner: {$ref: "users", $id: "testuser1"},
    members: [{$ref: "users", $id: "testuser2"}]
});
