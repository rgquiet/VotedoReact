const home = 'http://localhost:8080/'; //'http://ec2-3-121-231-202.eu-central-1.compute.amazonaws.com:8080/'
const api = home + 'api/';
let signIn = false;

$(function() {
    if(window.sessionStorage.getItem('displayPage')) {
        showMe(window.sessionStorage.getItem('displayPage'));
    } else {
        onPublicSession('');
    }
    checkAccessToken();
});

function showMe(pageName) {
    // wip: Not so pretty
    if(pageName === 'home') {
        onPublicSession('');
    }
    // Switch display to hidden and show required page
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
                /*
                if(response['imgUrl']) {
                    $('#userImg').attr('src', response['imgUrl']);
                    $('#userImg').parent().removeClass('hidden');
                }
                */
                if(response['sessionId']) {
                    alreadyInSession(response['sessionId']);
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
function onPublicSession(name) {
    // Empty string is not allowed
    if(name === '') {
        name = '_';
    }
    $.ajax({
        type: 'GET',
        url: api + 'session/findPublic/' + name,
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        success: function(response) {
            $('#publicSessionList').empty();
            $.each(response, function(i) {
                $('#publicSessionList').append(
                    '<ion-item button onclick="onJoinSession(' +
                    "'" + response[i].id + "'" +
                    ');"><ion-thumbnail><img src="' +
                    response[i].ownerImg +
                    '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                    response[i].name +
                    '</h2><p>' +
                    response[i].owner +
                    '</p></ion-label><ion-label class="ion-text-end ion-margin-end">' +
                    '<ion-icon name="people-outline" slot="icon-only"></ion-icon>' +
                    response[i].members +
                    '</ion-label></ion-item>'
                );
            });
        }
    });
}

function onJoinSession(sessionId) {
    const id = window.sessionStorage.getItem('userId');
    if(id) {
        $.ajax({
            type: 'POST',
            url: api + 'session/join',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({
                id: id,
                sessionId: sessionId
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
                    // wip...
                    alert('You are already in a session');
                }
            },
            success: function(response) {
                showSessionContent(response);
                showMe('session');
            }
        });
    } else {
        alert('Please sign in to join a session');
    }
}

/* Page: 'create' */
$('#sessionName').on('input', function() {
    if($(this).val().length > 2 && $(this).val().length < 26) {
        $('#sessionNameWarning').addClass('hidden');
        $('#sessionCreate').prop('disabled', false);
    } else {
        $('#sessionNameWarning').removeClass('hidden');
        $('#sessionCreate').prop('disabled', true);
    }
});

$('#sessionOpen').on('click', function() {
    if($(this).attr('class').search('checkbox-checked') === -1) {
        $.ajax({
            type: 'GET',
            url: api + 'user/friends/' + sessionStorage.getItem('userId'),
            contentType: 'application/json; charset=utf-8',
            success: function(response) {
                if(response.length > 0) {
                    $.each(response, function(i) {
                        $('#sessionFriends').append(
                            '<ion-item><ion-thumbnail><img src="' +
                            response[i].imgUrl +
                            '"/></ion-thumbnail><ion-label class="ion-margin-start">' +
                            response[i].username +
                            '</ion-label><ion-checkbox id="' +
                            response[i].id +
                            '" slot="end"></ion-checkbox></ion-item>'
                        );
                    });
                } else {
                    $('#sessionFriends').append(
                        '<ion-item><ion-label class="ion-text-center">' +
                        "I'm sorry, you have no friends :(" +
                        '</ion-label></ion-item>'
                    );
                }
            }
        });
    } else {
        $('#sessionFriends').empty();
    }
});

function onCreateSession() {
    let invitations = [];
    if($('#sessionOpen').attr('class').search('checkbox-checked') !== -1) {
        $('#sessionFriends .checkbox-checked').each(function() {
            invitations.push($(this).attr('id'));
        });
    } else {
        invitations = null;
    }
    $.ajax({
        type: 'POST',
        url: api + 'session/create',
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            userId: window.sessionStorage.getItem('userId'),
            name: $('#sessionName').val(),
            invitations: invitations
        }),
        statusCode: {
            403: function(xhr) {
                console.log(xhr['responseText']);
                // wip...
                alert('You are already in a session');
            }
        },
        success: function(response) {
            showSessionContent(response);
            showMe('session');
        }
    });
}

/* Page: 'session' */
function alreadyInSession(id) {
    $.ajax({
        type: 'GET',
        url: api + 'session/' + id,
        contentType: 'application/json; charset=utf-8',
        statusCode: {
            403: function (xhr) {
                console.log(xhr['responseText']);
            }
        },
        success: function(response) {
            showSessionContent(response);
            showMe('session');
        }
    });
}

function showSessionContent(session) {
    // wip...
    console.log(session);
}
