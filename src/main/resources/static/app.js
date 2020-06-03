const home = 'http://localhost:8080/'; //'http://ec2-3-121-231-202.eu-central-1.compute.amazonaws.com:8080/'
const api = home + 'api/';
let sse = null;

$(window).on('beforeunload', function() {
    if(sse) {
        sse.close();
    }
});

$(function() {
    if(window.sessionStorage.getItem('displayPage')) {
        checkAccessToken();
    } else {
        onPublicSession('');
    }
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
                accessToken: params.get('access_token'),
                timestamp: new Date().getTime() + Number(params.get('expires_in')) * 1000
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
                    window.location.replace(home);
                }
            },
            success: function(response) {
                window.sessionStorage.setItem('userId', response['id']);
                showMe(window.sessionStorage.getItem('displayPage'));
                $('#userName').text(response['username']);
                $('#logInOut').text('Logout');
                /*
                if(response['imgUrl']) {
                    $('#userImg').attr('src', response['imgUrl']);
                    $('#userImg').parent().removeClass('hidden');
                }
                */
                sse = new EventSource(api + 'user/sub/' + response['id']);
                sse.onmessage = function(event) { onEvent(event); }
                if(response['sessionId']) {
                    alreadyInSession(response['sessionId']);
                }
            }
        });
    } else {
        window.sessionStorage.clear();
        onPublicSession('');
    }
}

function implicitGrantFlow(pageName) {
    if(window.sessionStorage.getItem('userId')) {
        showMe(pageName);
    } else {
        window.sessionStorage.setItem('displayPage', pageName);
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

function logInOut(btn) {
    if(btn.text() === 'Login') {
        implicitGrantFlow('home');
    } else {
        window.location.replace(home);
    }
}

function showMe(pageName) {
    if(pageName === 'home') {
        onPublicSession('');
    } else if(pageName === 'friends') {
        onMyFriends();
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

function onEvent(event) {
    // wip...
    console.log(event);
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
                        $('#sessionFriendsList').append(
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
                    $('#sessionFriendsList').append(
                        '<ion-item><ion-label class="ion-text-center">' +
                        "I'm sorry, you have no friends :(" +
                        '</ion-label></ion-item>'
                    );
                }
            }
        });
    } else {
        $('#sessionFriendsList').empty();
    }
});

function onCreateSession() {
    let invitations = [];
    if($('#sessionOpen').attr('class').search('checkbox-checked') !== -1) {
        $('#sessionFriendsList .checkbox-checked').each(function() {
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

/* Page: 'friends' */
let currentModal = null;

async function createModal() {
    const modal = await modalController.create({
        component: 'modal-content'
    });
    await modal.present();
    currentModal = modal;
}

function dismissModal() {
    if(currentModal) {
        currentModal.dismiss().then(() => { currentModal = null; });
    }
    onMyFriends();
}

customElements.define('modal-content', class ModalContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <ion-header>
                <ion-toolbar>
                    <ion-title class="ion-text-center">Search Friends</ion-title>
                    <ion-buttons slot="end">
                        <ion-button onclick="dismissModal()">
                            <ion-icon name="close-outline"></ion-icon>
                        </ion-button>
                    </ion-buttons>
                </ion-toolbar>
            </ion-header>
            <ion-content class="ion-padding">
                <ion-searchbar oninput="onSearchFriend($(this).val());"></ion-searchbar>
                <ion-list id="userList"></ion-list>
            </ion-content>
        `;
        onSearchFriend('');
    }
});

function onSearchFriend(name) {
    // Empty string is not allowed
    if(name === '') {
        name = '_';
    }
    $.ajax({
        type: 'GET',
        url: api + 'user/findUser/' + name,
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        success: function(response) {
            $('#userList').empty();
            $.each(response, function(i) {
                $('#userList').append(
                    '<ion-item button onclick="onInviteFriend(' +
                    "'" + response[i].id + "'" +
                    ');"><ion-thumbnail><img src="' +
                    response[i].imgUrl +
                    '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                    response[i].username +
                    '</h2></ion-label></ion-item>'
                );
            });
        }
    });
}

function onMyFriends() {
    // wip: Use this as callback after implicitGrantFlow
    $.ajax({
        type: 'GET',
        url: api + 'user/friends/' + sessionStorage.getItem('userId'),
        contentType: 'application/json; charset=utf-8',
        success: function(response) {
            $('#myFriendsList').empty();
            if(response.length > 0) {
                $.each(response, function(i) {
                    $('#myFriendsList').append(
                        '<ion-item id="' +
                        response[i].id +
                        '"><ion-thumbnail><img src="' +
                        response[i].imgUrl +
                        '"/></ion-thumbnail><ion-label class="ion-margin-start">' +
                        response[i].username +
                        '</ion-label></ion-item>'
                    );
                });
            } else {
                $('#myFriendsList').append(
                    '<ion-item lines="none"><ion-label class="ion-text-center">' +
                    "I'm sorry, you have no friends :(" +
                    '</ion-label></ion-item>'
                );
            }
        }
    });
}

function onInviteFriend(friend) {
    $.ajax({
        type: 'POST',
        url: api + 'user/friend',
        dataType: 'text',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            id: window.sessionStorage.getItem('userId'),
            friendId: friend
        }),
        statusCode: {
            403: function(xhr) {
                console.log(xhr['responseText']);
            }
        },
        success: function(response) {
            console.log(response);
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
            404: function(xhr) {
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
