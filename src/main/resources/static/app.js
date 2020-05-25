const home = 'http://localhost:8080/'; //'http://ec2-3-121-231-202.eu-central-1.compute.amazonaws.com:8080/'
const api = home + 'api/';
let subs = [];
let signIn = false;

$(function() {
    if(window.sessionStorage.getItem('displayPage')) {
        showMe(window.sessionStorage.getItem('displayPage'));
    }
    checkAccessToken();
    subPublicSession();
});

function showMe(pageName) {
    $('.display').each(function() {
        $(this).removeClass('display');
        $(this).addClass('hidden');
        // Close SSE for publicSession if active
        if($(this).attr('id') === 'page-home') {
            checkSubAndClose('session/subPublic', true);
        }
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

function checkSubAndClose(sse, close) {
    let active = false;
    subs.forEach(function(sub, i) {
        if(sub.url.search(sse) !== -1) {
            if(close) {
                sub.close();
                subs.splice(i);
            } else {
                active = true;
            }
        }
    });
    return active;
}

/* Page: 'home' */
function subPublicSession() {
    let sse = 'session/subPublic';
    if(!checkSubAndClose(sse, false)) {
        let sub = new EventSource(api + sse);
        sub.onmessage = function(event) {
            console.log(event.data);
            const response = JSON.parse(event.data);
            $('#publicSessionList').append(
                '<ion-item button onclick="onJoinSession(' +
                "'" + response.id + "'" +
                ');"><ion-thumbnail><img src="' +
                response.ownerImg +
                '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                response.name +
                '</h2><p>' +
                response.owner +
                '</p></ion-label><ion-label class="ion-text-end ion-margin-end">' +
                '<ion-icon name="people-outline" slot="icon-only"></ion-icon>' +
                response.members +
                '</ion-label></ion-item>'
            );
        };
        subs.push(sub);
    }
}

function onJoinSession(id) {
    const userId = window.sessionStorage.getItem('userId');
    if(userId) {
        $.ajax({
            type: 'POST',
            url: api + 'session/join',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({
                userId: userId,
                sessionId: id
            }),
            statusCode: {
                403: function (xhr) {
                    console.log(xhr['responseText']);
                    showMe('session');
                }
            },
            success: function (response) {
                // wip...
                console.log(response);
                showMe('session');
            }
        });
    } else {
        alert('Please sign in to join a session');
    }
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
    if($(this).attr('class').search('checkbox-checked') === -1) {
        $.ajax({
            type: 'GET',
            url: api + 'user/friends/' + sessionStorage.getItem('userId'),
            contentType: 'application/json; charset=utf-8',
            success: function(response) {
                if(response.length > 0) {
                    $.each(response, function(i){
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
