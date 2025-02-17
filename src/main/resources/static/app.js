const home = 'http://localhost:8080/'; //'http://ec2-3-121-231-202.eu-central-1.compute.amazonaws.com:8080/'
const api = home + 'api/';
const timeout = 1000;
let progress = 0;
let rate = 0;
let interval = null;
let userSse = null;
let sessionSse = null;
let currentAlert = null;
let currentModal = null;

$(window).on('beforeunload', function() {
    if(userSse) {
        userSse.close();
    }
});

$(function() {
    if(sessionStorage.getItem('displayPage')) {
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
                403: function() {
                    window.location.replace(home);
                }
            },
            success: function(response) {
                sessionStorage.setItem('userId', response['id']);
                showMe(sessionStorage.getItem('displayPage'));
                $('#logInOut').text('Logout');
                $('#userName').text(response['username']);
                $('#userImg').attr('src', response['imgUrl']);
                $('#userImg').parent().removeClass('ion-hide');
                userSse = new EventSource(api + 'user/sub/' + response['id']);
                userSse.onmessage = function(event) { onUserEvent(event); }
                if(response['sessionId']) {
                    updateVotes(response['votes']);
                    alreadyInSession(response['sessionId']);
                }
            }
        });
    } else {
        sessionStorage.clear();
        onPublicSession('');
    }
}

function implicitGrantFlow(pageName) {
    if(sessionStorage.getItem('userId')) {
        showMe(pageName);
    } else {
        sessionStorage.setItem('displayPage', pageName);
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

function toggleFooter() {
    if($('.toggle').attr('id') === 'bar-main') {
        $('#bar-main').removeClass('toggle');
        $('#bar-main').addClass('ion-hide');
        $('#bar-session').removeClass('ion-hide');
        $('#bar-session').addClass('toggle');
        showMe('session');
    } else {
        $('#bar-session').removeClass('toggle');
        $('#bar-session').addClass('ion-hide');
        $('#bar-main').removeClass('ion-hide');
        $('#bar-main').addClass('toggle');
    }
}

function showMe(pageName) {
    if(pageName === 'home') {
        onPublicSession('');
    } else if(pageName === 'friends') {
        onMyFriends();
    }
    if(pageName === 'create') {
        createEventOn();
    } else {
        createEventOff();
    }
    // Switch display to hidden and show required page
    $('.display').each(function() {
        $(this).removeClass('display');
        $(this).addClass('ion-hide');
    });
    $('#page-' + pageName).addClass('display');
    $('#page-' + pageName).removeClass('ion-hide');
    sessionStorage.setItem('displayPage', pageName);
}

function onUserEvent(event) {
    const response = JSON.parse(event.data);
    $('#invitations').append(
        '<ion-card><ion-card-content class="ion-text-center" style="padding-bottom: 0px;">' +
        '<h2 style="font-weight: bold; padding-bottom: 5px;">Session Invitation</h2>' +
        response['message'] +
        '</ion-card-content><ion-item id="' +
        response['id'] +
        '"><ion-button fill="outline" slot="start" onclick="acceptInvitation($(this));">' +
        '<ion-icon name="checkmark-outline"></ion-icon>accept</ion-button>' +
        '<ion-button fill="outline" slot="end" onclick="rejectInvitation($(this));">' +
        '<ion-icon name="close-outline"></ion-icon>reject</ion-button></ion-item></ion-card>'
    );
}

function closeUserEvent(sessionId) {
    const id = sessionStorage.getItem('userId');
    $.ajax({
        type: 'POST',
        url: api + 'user/closeEvent',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            id: id,
            sessionId: sessionId
        })
    });
}

function acceptInvitation(tag) {
    let sessionId = tag.parent().attr('id');
    onJoinSession(sessionId);
    closeUserEvent(sessionId);
    tag.parent().parent().remove();
}

function rejectInvitation(tag) {
    let sessionId = tag.parent().attr('id');
    closeUserEvent(sessionId);
    tag.parent().parent().remove();
}

async function showAlert(header, message, buttons) {
    const alert = await alertController.create({
        header: header,
        message: message,
        buttons: buttons
    });
    await alert.present();
}

function dismissAlert() {
    if(currentAlert) {
        currentAlert.dismiss();
        currentAlert = null;
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
    const id = sessionStorage.getItem('userId');
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
                    if(xhr['responseText'] === 'Session closed') {
                        onPublicSession('');
                        showAlert('Attention', 'This session has already been closed', ['OK']);
                    } else {
                        showAlert('Attention', 'You are already in a session', ['OK']);
                    }
                }
            },
            success: function(response) {
                showSessionContent(response);
                showMe('session');
            }
        });
    } else {
        showAlert('Attention', 'Please sign in to join a session', [
            {
                text: 'Cancel',
                role: 'cancel',
            }, {
                text: 'Sign in',
                handler: () => { implicitGrantFlow('home'); }
            }
        ]);
    }
}

/* Page: 'create' */
function createEventOn() {
    $('#sessionDevice').on('ionChange', checkSessionCreate);
    $('#sessionName').on('input', onSessionName);
    $('#sessionOpen').on('click', onSessionOpen);
}

function createEventOff() {
    $('#sessionDevice').off('ionChange');
    $('#sessionName').off('input');
    $('#sessionOpen').off('click');
}

function checkSessionDevice() {
    return $('#sessionDevice').val() !== 'Audio Device' && $('#sessionDevice').val() !== '';
}

function checkSessionName() {
    return $('#sessionName').val().length > 2 && $('#sessionName').val().length < 26;
}

function checkSessionCreate() {
    $('#sessionCreate').prop(
        'disabled',
        !(checkSessionDevice() && checkSessionName())
    );
}

function onSessionName() {
    if(!checkSessionName()) {
        $('#sessionNameWarning').removeClass('ion-hide');
    } else {
        $('#sessionNameWarning').addClass('ion-hide');
    }
    checkSessionCreate();
}

function onSessionOpen() {
    if($(this).attr('class').search('toggle-checked') === -1) {
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
}

function onAudioDevice() {
    $.ajax({
        type: 'GET',
        url: api + 'spotify/devices/' + sessionStorage.getItem('userId'),
        contentType: 'application/json; charset=utf-8',
        statusCode: {
            403: function() {
                $('ion-alert')[0].dismiss();
                showAlert('Attention', 'Please start spotify on any device', ['OK']);
            }
        },
        success: function(response) {
            $('#sessionDevice').empty();
            $.each(response, function(i) {
                $('#sessionDevice').append(
                    '<ion-select-option value="' +
                    response[i].id +
                    '">' +
                    response[i].name +
                    '</ion-select-option>'
                );
            });
        }
    });
}

function onCreateSession() {
    let invitations = [];
    if($('#sessionOpen').attr('class').search('toggle-checked') !== -1) {
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
            userId: sessionStorage.getItem('userId'),
            deviceId: $('#sessionDevice').val(),
            name: $('#sessionName').val(),
            invitations: invitations
        }),
        statusCode: {
            403: function(xhr) {
                if(xhr['responseText'] === 'Premium needed') {
                    showAlert('Attention', 'You must have spotify premium to open a session', ['OK']);
                } else {
                    showAlert('Attention', 'You are already in a session', ['OK']);
                }
            }
        },
        success: function(response) {
            showSessionContent(response);
            showMe('session');
        }
    });
}

/* Page: 'friends' */
async function createFriendModal() {
    const modal = await modalController.create({
        component: 'modal-content-friend'
    });
    await modal.present();
    currentModal = modal;
}

function dismissFriendModal() {
    if(currentModal) {
        currentModal.dismiss().then(() => { currentModal = null; });
    }
    onMyFriends();
}

customElements.define('modal-content-friend', class ModalContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <ion-header>
                <ion-toolbar>
                    <ion-title class="ion-text-center center-modal-title">Find Friends</ion-title>
                    <ion-buttons slot="end">
                        <ion-button onclick="dismissFriendModal();">
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
        contentType: 'application/json; charset=utf-8',
        success: function(response) {
            $('#userList').empty();
            $.each(response, function(i) {
                if(response[i].id !== sessionStorage.getItem('userId')) {
                    $('#userList').append(
                        '<ion-item button onclick="onInviteFriend(' +
                        "'" + response[i].id + "'" +
                        ');"><ion-thumbnail><img src="' +
                        response[i].imgUrl +
                        '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                        response[i].username +
                        '</h2></ion-label></ion-item>'
                    );
                }
            });
        }
    });
}

function onMyFriends() {
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
                        '</ion-label><ion-button fill="outline" slot="end" ' +
                        'onclick="onFriendRemove($(this));">Remove</ion-button></ion-item>'
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
        url: api + 'user/addFriend',
        dataType: 'text',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            id: sessionStorage.getItem('userId'),
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

function onFriendRemove(tag) {
    $.ajax({
        type: 'POST',
        url: api + 'user/removeFriend',
        dataType: 'text',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            id: sessionStorage.getItem('userId'),
            friendId: tag.parent().attr('id')
        }),
        statusCode: {
            403: function(xhr) {
                console.log(xhr['responseText']);
            }
        },
        success: function() {
            onMyFriends();
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
    // Change header and footer
    $('#sessionTitle').text(session['name']);
    if(session['votes'] !== null) {
        updateVotes(session['votes']);
    }
    $('#votedo').addClass('ion-hide');
    $('#header-session').removeClass('ion-hide');
    toggleFooter();
    onTrackStart(session['track']);
    $('[name="toggleUp"]').removeClass('ion-hide');
    // Listen to session events
    sessionSse = new EventSource(api + 'session/sub/' + session['id']);
    sessionSse.onmessage = function(event) { onSessionEvent(event); }
    // Get all tracks in this session
    $.ajax({
        type: 'GET',
        url: api + 'session/getTracks/' + session['id'],
        contentType: 'application/json; charset=utf-8',
        success: function(response) {
            $('#sessionTrackList').empty();
            // Sort tracks in descending order based on votes
            response.sort(function(a, b) {
                return parseInt(b.votes) - parseInt(a.votes);
            });
            $.each(response, function(i) {
                addToSessionTrackList(response[i]);
            });
        }
    });
}

function hideSessionContent() {
    // Change header and footer
    $('#votedo').removeClass('ion-hide');
    $('#header-session').addClass('ion-hide');
    if($('#bar-session').hasClass('toggle')) {
        toggleFooter();
    }
    $('[name="toggleUp"]').addClass('ion-hide');
}

async function createTrackModal() {
    const modal = await modalController.create({
        component: 'modal-content-track'
    });
    await modal.present();
    currentModal = modal;
}

function dismissTrackModal() {
    if(currentModal) {
        currentModal.dismiss().then(() => { currentModal = null; });
    }
}

customElements.define('modal-content-track', class ModalContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <ion-header>
                <ion-toolbar>
                    <ion-title class="ion-text-center center-modal-title">Search Track</ion-title>
                    <ion-buttons slot="end">
                        <ion-button onclick="dismissTrackModal();">
                            <ion-icon name="close-outline"></ion-icon>
                        </ion-button>
                    </ion-buttons>
                </ion-toolbar>
            </ion-header>
            <ion-content class="ion-padding">
                <ion-searchbar oninput="onSearchTrack($(this).val());"></ion-searchbar>
                <ion-list id="trackList"></ion-list>
            </ion-content>
        `;
    }
});

function msToTime(ms) {
    let minutes = Math.floor(ms / 60000);
    let seconds = ((ms % 60000) / 1000).toFixed(0);
    return minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
}

function onSearchTrack(name) {
    // Empty string is not allowed
    if(name !== '') {
        $.ajax({
            type: 'GET',
            url: api + 'spotify/findTracks/' + name,
            contentType: 'application/json; charset=utf-8',
            success: function(response) {
                $('#trackList').empty();
                $.each(response, function(i) {
                    let time = msToTime(response[i].timeMs);
                    $('#trackList').append(
                        '<ion-item button onclick="onSelectTrack({' +
                        "'" + 'id' + "'" + ':' +
                        "'" + response[i].id + "'," +
                        "'" + 'name' + "'" + ':' +
                        "'" + response[i].name.replace("'", "") + "'," +
                        "'" + 'artist' + "'" + ':' +
                        "'" + response[i].artist.replace("'", "") + "'," +
                        "'" + 'imgUrl' + "'" + ':' +
                        "'" + response[i].imgUrl + "'," +
                        "'" + 'timeMs' + "'" + ':' +
                        response[i].timeMs +
                        '}); dismissTrackModal();"><ion-thumbnail><img src="' +
                        response[i].imgUrl +
                        '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
                        response[i].name +
                        '</h2><p>' +
                        response[i].artist +
                        '</p></ion-label><ion-label class="ion-text-end ion-margin-end"><p>' +
                        time +
                        '</p></ion-label></ion-item>'
                    );
                });
            }
        });
    }
}

function onSelectTrack(track) {
    $.ajax({
        type: 'POST',
        url: api + 'user/track/' + sessionStorage.getItem('userId'),
        dataType: 'text',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify(track),
        statusCode: {
            403: function(xhr) {
                if(xhr['responseText'] === 'Track already used') {
                    showAlert('Attention', 'This track was already chosen in this session', ['OK']);
                } else {
                    showAlert('Attention', 'You can only choose one track at a time', ['OK']);
                }
            }
        },
        success: function(response) {
            console.log(response);
        }
    });
}

function updateVotes(votes) {
    $('#myVotes').empty().append(
        '<ion-icon name="hand-left-outline"></ion-icon>' +
        votes
    );
}

function incVotes() {
    let votes = parseInt($('#myVotes').text());
    updateVotes(votes + 1);
}

function onFlameHost() {
    // wip...
    console.log('Fuck you host!');
}

function onRestartSession(stop) {
    $.ajax({
        type: 'POST',
        url: api + 'session/restart',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify(stop)
    });
}

function onLeaveSession() {
    sessionSse.close();
    $.ajax({
        type: 'POST',
        url: api + 'session/leave/' + sessionStorage.getItem('userId'),
        contentType: 'application/json; charset=utf-8',
        success: function() {
            hideSessionContent();
            showMe('home');
        }
    });
}

/* Event Handling */
function onSessionEvent(event) {
    const response = JSON.parse(event.data);
    switch(response['type']) {
        case 'TRACKCREATE':
            addToSessionTrackList(response['dto']);
            break;
        case 'TRACKREMOVE':
            onTrackRemove(response['dto']);
            break;
        case 'TRACKSTART':
            dismissAlert();
            onTrackStart(response['dto']);
            $('#sessionTrackList ion-button').each(function() {
                $(this).prop('disabled', false);
            });
            break;
        case 'VOTETRACK':
            onVoteTrack(response['dto']);
            break;
        case 'VOTEUPDATE':
            if(response['dto'].id === sessionStorage.getItem('userId')) {
                updateVotes(response['dto'].votes);
            }
            break;
        case 'VOTESTOP':
            $('#sessionTrackList ion-button').each(function() {
                $(this).prop('disabled', true);
            });
            incVotes();
            break;
        case 'SESSIONSTOP':
            onSessionStop(response['dto']);
            break;
        case 'SESSIONCLOSE':
            onSessionClose(response['dto']);
            break;
    }
}

function addToSessionTrackList(track) {
    let time = msToTime(track.timeMs);
    let html =
        '<ion-item><ion-thumbnail><img src="' +
        track.imgUrl +
        '"/></ion-thumbnail><ion-label class="ion-margin-start"><h2>' +
        track.name +
        '</h2><p>' +
        track.artist +
        '</p></ion-label><ion-label class="ion-text-center ion-hide-md-down"><p>' +
        time +
        '</p></ion-label><ion-label class="trackVote ion-text-end">' +
        '<ion-icon name="hand-left-outline"></ion-icon>' +
        track.votes +
        '</ion-label><ion-button id="' +
        track.id +
        '" fill="outline" slot="end" style="width: 73px; margin-left: 10px;"';
    if(track.userId === sessionStorage.getItem('userId')) {
        $('#sessionTrackList').append(
            html + 'onclick="revokeMyTrack($(this));">Revoke</ion-button></ion-item>'
        );
    } else {
        $('#sessionTrackList').append(
            html + 'onclick="vote($(this));">Vote</ion-button></ion-item>'
        );
    }
}

function revokeMyTrack(tag) {
    showAlert('Attention', 'Do you really want to revoke your track?', [
        {
            text: 'Yes',
            handler: () => {
                $.ajax({
                    type: 'POST',
                    url: api + 'session/revokeTrack',
                    contentType: 'application/json; charset=utf-8',
                    data: JSON.stringify({
                        userId: sessionStorage.getItem('userId'),
                        trackId: tag.attr('id')
                    })
                });
            }
        }, {
            text: 'No',
            role: 'cancel'
        }
    ]);
}

function vote(tag) {
    if(parseInt($('#myVotes').text()) === 0) {
        showAlert('Attention', 'You have no votes left', ['OK'])
    } else {
        $.ajax({
            type: 'POST',
            url: api + 'session/vote',
            dataType: 'text',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({
                userId: sessionStorage.getItem('userId'),
                trackId: tag.attr('id')
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
                }
            },
            success: function(response) {
                updateVotes(response);
            }
        });
    }
}

function onTrackRemove(track) {
    $('#' + track).parent().remove();
}

function onTrackStart(track) {
    let time = new Date().getTime() - track.startMs;
    $('#currentTrackInfo').empty().append(
        '<ion-thumbnail slot="start"><img src="' +
        track.imgUrl +
        '"/></ion-thumbnail><ion-label style="min-width:' +
        track.name.length * 8 + 'px; max-width:' +
        track.name.length * 9 + 'px;">' +
        track.name +
        '</h2><p>' +
        track.artist +
        '</p></ion-label><ion-label class="ion-text-end ion-margin-end ion-hide-md-down"' +
        'style="min-width: 40px;"><p id="currentTime" name="' + time + '">' +
        msToTime(time) +
        '</p></ion-label><ion-progress-bar class="ion-hide-md-down"></ion-progress-bar>' +
        '<ion-label class="ion-margin-start" style="min-width: 40px;"><p>' +
        msToTime(track.timeMs) +
        '</p></ion-label>'
    );
    progress = time / track.timeMs;
    rate = timeout / track.timeMs;
    if(interval !== null) {
        clearInterval(interval);
    }
    interval = setInterval(updateProgress, timeout);
}

function updateProgress() {
    progress = progress + rate;
    if(progress < 1) {
        let time = parseInt($('#currentTime').attr('name')) + timeout;
        $('#currentTime').attr('name', time);
        $('#currentTime').text(msToTime(time));
        $('ion-progress-bar').each(function() {
            $(this).val(progress);
        });
    } else {
        clearInterval(interval);
        interval = null;
    }
}

function onVoteTrack(track) {
    let tag = $('#' + track['id']).parent();
    tag.remove();
    tag.children('.trackVote').empty().append(
        '<ion-icon name="hand-left-outline"></ion-icon>' +
        track['votes']
    );
    let insert = false;
    $('#sessionTrackList').children().each(function() {
        let vote = parseInt($(this).children('.trackVote').text());
        if(track['votes'] >= vote) {
            tag.insertBefore($(this));
            insert = true;
            return false;
        }
    });
    if(!insert) {
        $('#sessionTrackList').append(tag);
    }
}

function onSessionStop(stop) {
    if(stop.ownerId === sessionStorage.getItem('userId')) {
        showAlert('Session stopped', 'You cheated!', [
            {
                text: 'Leave',
                role: 'cancel',
                handler: () => { onLeaveSession(); }
            }, {
                text: 'Continue',
                handler: () => { onRestartSession(stop); }
            }
        ]);
    } else {
        showAlert('Attention', stop.sessionName + ' was stopped by the host', [
            {
                text: 'Leave',
                role: 'cancel',
                handler: () => { onLeaveSession(); }
            }, {
                text: 'Flame host',
                handler: () => { onFlameHost(); }
            }
        ]);
        currentAlert = $('ion-alert')[0];
    }
    if(interval !== null) {
        clearInterval(interval);
        interval = null;
    }
}

function onSessionClose(name) {
    sessionSse.close();
    hideSessionContent();
    showMe('home');
    showAlert('Attention', name + ' was closed by the host', ['OK']);
}
