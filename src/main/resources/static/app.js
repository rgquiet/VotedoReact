$(document).ready(function() {
    $.ajax({
        url: "http://localhost:8080/api/user/testUser",
        contentType: "application/json",
        dataType: 'json',
        success: function(result) {
            $('#userName').text(result.username);
        }
    });
});
