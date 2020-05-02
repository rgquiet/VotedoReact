const api = 'http://localhost:8080/api/';
let signIn = false;

$(function() {
    checkAccessToken();
    // wip: Subscribe public sessions
});

function checkAccessToken() {
    let url = window.location.href.replace('#', '?');
    let params = (new URL(url)).searchParams;
    if(params.get('access_token')) {
        $.ajax({
            type: 'POST',
            url: api + 'spotify/token',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({
                accessToken: params.get('access_token')
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
                }
            },
            success: function(response) {
                console.log(response);
                signIn = true;
                $('#collapse1').collapse('show');
                // wip: $('#userName').text(response['username']);
            }
        });
    }
}

function implicitGrantFlow() {
    if(!signIn) {
        $.ajax({
            type: 'GET',
            url: api + 'spotify/auth',
            dataType: 'text',
            success: function(url) {
                window.location.replace(url);
            }
        });
    }
}
