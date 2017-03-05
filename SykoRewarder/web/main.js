var channelId = "";
var videoId = youtube_parser(window.location);
var likeButton = null;
var subButton = null;
var username = "";
var iDiv = null;
var relooping = false;
var userset = "";
var commentButton = null; //It can be obtained only in videos
var main_channel = null; //It can be obtained in videos & channel
var host_channel = null;

window.addEventListener("spfdone", rehook);
window.addEventListener("load", hook);
window.addEventListener('spfclick', function() {
    window.location.reload(true);
    return false;
});

function hook() 
{
    console.log("Tampermonkey hook!");
    iniciar();
}

function rehook() 
{
    console.log("Rehooking...");
    hook();
    var useralert = document.getElementById("useralert");
    if(useralert != null && useralert.style.display != "none") 
        useralert.style.display = "none";
}

function iniciar() 
{
    getScript('https://code.jquery.com/jquery-latest.min.js', init);
}

function init() 
{

    iDiv = document.createElement('div');
    iDiv.id = 'sykonotification';
    iDiv.style.position = "fixed";
    iDiv.style.top = "60px";
    iDiv.style.left = "50%";
    iDiv.style.width = "auto";
    iDiv.style.height = "auto";
    iDiv.style.padding = "10px";
    iDiv.style.color = "white";
    iDiv.style.backgroundColor = "#167AC6";
    iDiv.style.zIndex = "10";
    iDiv.style.textAlign = "center";
    iDiv.style.fontWeight = "500";
    iDiv.style.display = "none";
    document.getElementsByTagName('body')[0].appendChild(iDiv);

    if(youtube_parser(window.location) != null)
        likeButton = document.getElementsByClassName("like-button-renderer-like-button-unclicked")[0];

    var a = document.getElementsByTagName("a");

    console.log("Trying finding the channel!");

    var findInterval = setInterval(function() {
        console.log("Starting new loop...");
        for(var b = 0; b < a.length; ++b)
            if(a[b].href.indexOf("channel") > -1 && a[b].hasAttribute("data-external-id")) 
            {
                channelId = a[b].getAttribute("data-external-id");
                break;
            }
        if(channelId.length > 0) 
        {
            console.log("Channel "+channelId+" found!");
            clearInterval(findInterval);
            nextinit();
        }
    }, 1000);        

}

function nextinit() 
{

    userset = getCookie("userset");

    if(likeButton != null)
        likeButton.addEventListener("click", function() {reward("like")});

    if(window.location.href.indexOf("user") > -1 || window.location.href.indexOf("channel") > -1 || youtube_parser(window.location.href) != null) 
    {
        var c = document.getElementsByClassName("yt-uix-button yt-uix-button-size-default yt-uix-button-has-icon no-icon-markup yt-uix-subscription-button yt-can-buffer yt-uix-button-subscribe-branded");
        for(var d = 0; d < c.length; ++d)
            if(c[d].hasAttribute("data-clicktracking")) 
            {
                subButton = c[d];
                break;
            }
    }

    if(subButton != null)
        subButton.onclick = function() {reward("sub"); return false;}

    var commInt = setInterval(function() {

        commentButton = document.getElementsByClassName("yt-sb-post")[0];

        if(commentButton != null) 
        {
            commentButton.onclick = function() {reward("comment"); return false;}
            clearInterval(commInt);
            console.log("commentButton set!");
        }

    }, 1000);

    var photo = document.getElementsByClassName("yt-uix-sessionlink yt-user-photo g-hovercard spf-link")[0];

    if(photo != null)
        main_channel = photo.getAttribute("data-ytid");

    var e = document.getElementsByTagName("meta");

    for(var f = 0; f < e.length; ++f) 
    {
        if(e[f].hasAttribute("itemprop") && e[f].getAttribute("itemprop") == "channelId" && e[f].hasAttribute("content")) 
        {
            host_channel = e[f].getAttribute("content");
            break;
        }
    }

    $.ajax({
        type: "POST",
        url: "https://sykoreward.dotcloudapp.com/checker.php",
        data: 'user_channel='+channelId+'&host_channel='+host_channel,
        dataType: "text",
        success: function(data) {
            console.log(data);
            if(data == "null") 
                document.body.insertAdjacentHTML('afterbegin', '<span id="useralert" class="yt-uix-button   yt-uix-sessionlink yt-uix-button-default yt-uix-button-size-default" style="position:fixed;top: 10px;right: 185px;z-index: 2000000000;height: 26px;" onclick="javascript:useralert();"><span style="display:block;position:relative;margin-top:7px;">Registrar usuario</span></span>');
            else
                username = data;
        }
    });

    console.log("Sykoland hooked correctly!");
    showNotification(((userset.length == 0) ? "✔ Sykoreward está listo para usarse." : "¡Usuario "+userset+" establecido!"));
    if(userset.length > 0)
    {
        console.log(userset+" established!");
        delCookie("userset");
    }

}

function reward(type) 
{
    if(username.length > 0) 
    {
        console.log("Rewarding "+username+"!");
        var url = 'user_channel='+channelId+'&video='+videoId+'&username='+username+'&type='+type;
        if(type == "sub")
            url += '&subbed_channel='+subbed_channel;
        else if(type == "comment")
            url += '&main_channel='+main_channel;
        $.ajax({
            type: "POST",
            url: "https://sykoreward.dotcloudapp.com/index.php",
            data: url,
            dataType: "text",
            success: function(data) {
                if(data.indexOf("#3") == -1) 
                {
                    if(relooping) 
                    {
                        relooping = false;
                        $('#sykonotification').hide(1000);
                    }
                    console.log("Evaluating: "+data);
                    eval(data);
                } 
                else 
                {
                    if(!relooping) 
                    {
                        relooping = true;
                        showNotification("Hubo un error al verificar tu última acción, por favor, no cierres esta ventana, hasta que esta advertencia deje de mostrarse.", 'warning', false);
                        $('#sykonotification').show(1000);
                    }
                    setTimeout(function() {reward(type)}, 1000);
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {
                console.log(xhr.responseText);
            }
        });
    }
}

function useralert() 
{
    var user = prompt("Pon tu nombre de usuario de Minecraft", "");
    if(user != null) 
    {
        if(user == "null")
            return;
        username = user;
        $.ajax({
            type: "POST",
            url: "https://sykoreward.dotcloudapp.com/checker.php",
            data: 'user_channel='+channelId+'&host_channel='+host_channel+'&username='+username,
            dataType: "text",
            success: function(data) {
                console.log(data);
                if(data == "set") 
                {
                    document.getElementById("useralert").style.display = "none";
                    setCookie("userset", username);
                    window.location.reload(true);
                } else
                    eval(data);
            }
        });
    }
}          

function youtube_parser(url)
{
    var regex = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
    var match = regex.exec(url);
    if (match && match[7].length == 11)
    {
        return match[7];
    }
    return null;
}

function getScript(url, success) {
    var script = document.createElement('script');
    script.src = url;
    var head = document.getElementsByTagName('head')[0],
        done = false;
    // Attach handlers for all browsers
    script.onload = script.onreadystatechange = function() {
      if (!done && (!this.readyState
           || this.readyState == 'loaded'
           || this.readyState == 'complete')) {
        done = true;
        success();
        script.onload = script.onreadystatechange = null;
        head.removeChild(script);
      }
    };
    head.appendChild(script);
}

function showNotification(msg, type, showit, delay) 
{
    var d = 2000;
    if(!(typeof delay === 'undefined'))
        d = delay;
    var c = "#167AC6";
    if(!(typeof type === 'undefined'))
        switch(type) 
        {
            case "info":
                c = "#167AC6";
                break;
            case "warning":
                c = "#C6C616";
                break;
            case "error":
                c = "#C61616";
                break;
            case "correct":
                c = "#16C616";
                break;
        }
        iDiv.style.backgroundColor = c;
    iDiv.innerHTML = msg;
    if(typeof showit === 'undefined' || (!(typeof showit === 'undefined') && showit))
        $('#sykonotification').show(1000).delay(d).hide(1000);
    setTimeout(function() {iDiv.style.marginLeft = "-"+($("#sykonotification").width()/2)+"px";}, 1500);
}

function setCookie(cname, cvalue) 
{
    document.cookie = cname + "=" + cvalue;
}

function getCookie(cname) 
{
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
    }
    return "";
}

function delCookie(cname) 
{
    document.cookie = cname+"=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}