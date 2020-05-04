const home = 'http://localhost:8080/'
const api = home + 'api/';
let signIn = false;

$(function() {
    checkAccessToken();
    subscribePublicSession();
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
                signIn = true;
                $('#userName').text(response['username']);
                if(response['imgUrl']) {
                    $('#userImg').attr('src', response['imgUrl']);
                    $('#userImg').parent().removeClass('hidden');
                }
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

function showMe(page) {
    $('.display').each(function() {
        $(this).removeClass('display');
        $(this).addClass('hidden');
    });
    let pageName = $(page).attr('name');
    $('#page-' + pageName).addClass('display');
    $('#page-' + pageName).removeClass('hidden');
}

/* Page: 'home' */
function subscribePublicSession() {
    // wip: https://www.w3schools.com/html/html5_serversentevents.asp
}

/* Page: 'create' */
function onCreateSession() {
    // wip: Send to backend
}
