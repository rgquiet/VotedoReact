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
                timestamp: Math.round(new Date().getTime() / 1000 + params.get('expires_in'))
            }),
            statusCode: {
                403: function(xhr) {
                    console.log(xhr['responseText']);
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

function onCreateSession() {
    $.ajax({
        type: 'POST',
        url: api + 'session/create',
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        // wip: if($('#sessionOpen').is(':checked'))
        data: JSON.stringify({
            userId: window.sessionStorage.getItem('userId'),
            name: $('#sessionName').val(),
            invitations: ['invitation1', 'invitation2']
        }),
        success: function(response) {
            console.log(response);
        }
    });
}

/* Page: 'session' */
