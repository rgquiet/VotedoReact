const home = 'http://localhost:8080/'
const api = home + 'api/';
let signIn = false;

$(function() {
    if(window.sessionStorage.getItem('displayPage')) {
        showMe(window.sessionStorage.getItem('displayPage'));
    }
    checkAccessToken();
});

function showMe(pageName) {
    $('.display').each(function() {
        $(this).removeClass('display');
        $(this).addClass('hidden');
    });
    $('#page-' + pageName).addClass('display');
    $('#page-' + pageName).removeClass('hidden');
    window.sessionStorage.setItem('displayPage', pageName);
}

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
                accessToken: params.get('access_token'),
                timestamp: new Date().getTime() + Number(params.get('expires_in')) * 1000
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
                    window.sessionStorage.clear();
                    window.location.reload(true);
                }
            },
            success: function(response) {
                signIn = true;
                window.sessionStorage.setItem('userId', response['id']);
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
    } else {
        window.sessionStorage.removeItem('displayPage')
    }
}

/* Page: 'home' */
function subscribePublicSession() {
    // wip: https://www.w3schools.com/html/html5_serversentevents.asp
}

/* Page: 'create' */
$('#sessionName').change(function() {
    if($(this).val().length > 2 && $(this).val().length < 26) {
        $('#sessionNameWarning').addClass('hidden');
        $('#sessionCreate').prop('disabled', false);
    } else {
        $('#sessionNameWarning').removeClass('hidden');
        $('#sessionCreate').prop('disabled', true);
    }
});

$('#sessionOpen').click(function() {
    if($(this).attr('class').search('checkbox-checked') == -1) {
        $.ajax({
            type: 'GET',
            url: api + 'user/friends/' + sessionStorage.getItem('userId'),
            contentType: 'application/json; charset=utf-8',
            success: function(response) {
                if(response.length > 0) {
                    $.each(response, function(i){
                        $('#sessionFriends').append(
                            '<ion-item id="' +
                            response[i].id +
                            '"><ion-thumbnail><ion-img src="' +
                            response[i].imgUrl +
                            '"></ion-img></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                            response[i].username +
                            '</h2></ion-label></ion-item>'
                        );
                    });
                } else {
                    $('#sessionFriends').append(
                        '<ion-item><ion-label class="ion-text-center"><h2>' +
                        "I'm sorry, you have no friends :(" +
                        '</h2></ion-label></ion-item>'
                    );
                }
            }
        });
    } else {
        $('#sessionFriends').empty();
    }
});

function onCreateSession() {
    $.ajax({
        type: 'POST',
        url: api + 'session/create',
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            userId: window.sessionStorage.getItem('userId'),
            name: $('#sessionName').val(),
            invitations: null
        }),
        statusCode: {
            403: function(xhr) {
                console.log(xhr['responseText']);
                showMe('session');
            }
        },
        success: function(response) {
            // wip...
            console.log(response);
            showMe('session');
        }
    });
}

/* Page: 'session' */
