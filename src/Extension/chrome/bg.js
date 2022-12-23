(function () {
    var requests = [];
    var blockedHosts = ["update.microsoft.com", "windowsupdate.com", "thwawte.com" ];
    var videoUrls = [ ".facebook.com|pagelet", "player.vimeo.com/", "instagram.com/p/"];
    var fileExts = ["3GP", "7Z", "AVI", "BZ2", "DEB", "DOC", "DOCX", "EXE", "GZ", "ISO",
    "MSI", "PDF", "PPT", "PPTX", "RAR", "RPM", "XLS", "XLSX", "SIT", "SITX", "TAR", "JAR", "ZIP", "XZ"];
    var vidExts = ["MP4", "M3U8", "F4M", "WEBM", "OGG", "MP3", "AAC", "FLV", "MKV", "DIVX",
    "MOV", "MPG", "MPEG", "OPUS"];
    var isMDMUp = true;
    var monitoring = true;
    var debug = false;
    var MDMHost = "http://127.0.0.1:2002";
    var disabled = false;
    var lastIcon;
    var lastPopup;
    var videoList = [];
    var mimeList = [];
    var hasNativeMessagingHost = false;
    var port = undefined;

    var log = function (msg) {
        if (debug) {
            try {
                log(msg);
            } catch (e) {
                log(e + "");
            }
        }
    }

    function postNativeMessage(message) {
        if (hasNativeMessagingHost && port) {
            log(JSON.stringify(message));
            try {
                port.postMessage(message);
            } catch (err) {
                log(err);
                hasNativeMessagingHost = false;
                port = undefined;
            }
        }
    }

    var processRequest = function (request, response) {
        if (shouldInterceptFile(request, response)) {
            var file = getAttachedFile(response);
            if (!file) {
                file = getFileFromUrl(response.url);
            }
            sendToMDM(request, response, file, false);
            return { cancel: true };//return { redirectUrl: "http://127.0.0.1:2002/204" };
        } else {
            checkForVideo(request, response);
        }
    };

    var sendToMDM = function (request, response, file, video) {
        log("sending to MDM: " + response.url);
        var data = "url=" + response.url + "\r\n";
        if (file) {
            data += "file=" + file + "\r\n";
        }
        for (var i = 0; i < request.requestHeaders.length; i++) {
            data += "req=" + request.requestHeaders[i].name + ":" + request.requestHeaders[i].value + "\r\n";
        }
        for (var i = 0; i < response.responseHeaders.length; i++) {
            data += "res=" + response.responseHeaders[i].name + ":" + response.responseHeaders[i].value + "\r\n";
        }
        data += "res=tabId:" + request.tabId + "\r\n";
        data += "res=realUA:" + navigator.userAgent + "\r\n";
        chrome.cookies.getAll({ "url": response.url }, function (cookies) {
            for (var i = 0; i < cookies.length; i++) {
                var cookie = cookies[i];
                data += "cookie=" + cookie.name + ":" + cookie.value + "\r\n";
            }
            log(data);

            port.postMessage({"message":(video ? "/video" : "/download")+"\r\n"+data});
            // var xhr = new XMLHttpRequest();
            // xhr.open('POST', MDMHost + (video ? "/video" : "/download"), true);
            // xhr.send(data);
        });
    };

    var sendRecUrl = function (urls, index, data) {
        if (index == urls.length - 1) {
            log(data);

            port.postMessage({"message":"/links"+"\r\n"+data});

            // var xhr = new XMLHttpRequest();
            // xhr.open('POST', MDMHost + "/links", true);
            // xhr.send(data);
            return;
        }
        var url = urls[index];
        data += "url=" + url + "\r\n";
        data += "res=realUA:" + navigator.userAgent + "\r\n";
        chrome.cookies.getAll({ "url": url }, function (cookies) {
            for (var i = 0; i < cookies.length; i++) {
                var cookie = cookies[i];
                data += "cookie=" + cookie.name + ":" + cookie.value + "\r\n";
            }
            data += "\r\n\r\n";
            sendRecUrl(urls, index + 1, data);
        });
    };

    var sendUrlsToMDM = function (urls) {
        if (urls && urls.length > 0) {
                sendRecUrl(urls, 0, "");
        }
    };

    var sendUrlToMDM = function (url) {
        log("sending to MDM: " + url);
        var data = "url=" + url + "\r\n";
        data += "res=realUA:" + navigator.userAgent + "\r\n";
        chrome.cookies.getAll({ "url": url }, function (cookies) {
            for (var i = 0; i < cookies.length; i++) {
                var cookie = cookies[i];
                data += "cookie=" + cookie.name + ":" + cookie.value + "\r\n";
            }

            port.postMessage({"message":"/download"+"\r\n"+data});

            // var xhr = new XMLHttpRequest();
            // xhr.open('POST', MDMHost + "/download", true);
            // xhr.send(data);
        });
    };

    var sendImageToMDM = function (info, tab) {
        if (info.mediaType) {
            if ("image" == info.mediaType) {
                if (info.srcUrl) {
                    url = info.srcUrl;
                }
            }
        }

        if (!url) {
            url = info.linkUrl;
        }
        if (!url) {
            url = info.pageUrl;
        }
        if (!url) {
            return;
        }
        sendUrlToMDM(url);
    };

    var sendLinkToMDM = function (info, tab) {
        var url = info.linkUrl;
        
        if (!url) {
            if (info.mediaType) {
                if ("video" == info.mediaType || "audio" == info.mediaType) {
                    if (info.srcUrl) {
                        url = info.srcUrl;
                    }
                }
            }
        }
        if (!url) {
            url = info.pageUrl;
        }
        if (!url) {
            return;
        }
        sendUrlToMDM(url);
    };

    var runContentScript = function (info, tab) {
        log("running content script");
        chrome.tabs.executeScript({
            file: 'contentscript.js'
        });
    };

    var isVideoMime = function (mimeText) {
        if(!mimeList){
            return false;
        }
        var mime = mimeText.toLowerCase();
        for (var i = 0; i < mimeList.length; i++) {
            if (mime.indexOf(mimeList[i]) != -1) {
                return true;
            }
        }
        return false;
    }

    var checkForVideo = function (request, response) {
        
        var mime = "";
        var video = false;
        var url = response.url;

        for (var i = 0; i < response.responseHeaders.length; i++) {
            if (response.responseHeaders[i].name.toLowerCase() == "content-type") {
                mime = response.responseHeaders[i].value.toLocaleLowerCase();
                break;
            }
        }

        

        if (mime.startsWith("audio/") || mime.startsWith("video/") ||
            mime.indexOf("mpegurl") > 0 || mime.indexOf("f4m") > 0 || isVideoMime(mime)) {
                log("Checking video mime: "+mime+" "+JSON.stringify(mimeList));
            video = true;
        }

        if (!video) {
            if (videoUrls) {
                for (var i = 0; i < videoUrls.length; i++) {
                    var arr = videoUrls[i].split("|");
                    var matched = true;
                    for (var j = 0; j < arr.length; j++) {
                        //console.log(arr[j]);
                        if (url.indexOf(arr[j]) < 0) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        video = true;
                        log(url)
                        break;
                    }
                }
            }
        }


        if (!video) {
            if (vidExts) {
                var file = getFileFromUrl(url);
                var ext = getFileExtension(file);
                if (ext) {
                    ext = ext.toUpperCase();
                }
                for (var i = 0; i < vidExts.length; i++) {
                    if (vidExts[i] == ext) {
                        video = true;
                        break;
                    }
                }
            }
        }

        if (video) {
            if (request.tabId != -1) {
                chrome.tabs.get
                    (
                    request.tabId,
                    function (tab) {
                        sendToMDM(request, response, tab.title, true);
                    }
                    );
            } else {
                sendToMDM(request, response, null, true);
            }
        }
    };

    var getAttachedFile = function (response) {
        for (var i = 0; i < response.responseHeaders.length; i++) {
            if (response.responseHeaders[i].name.toLowerCase() == 'content-disposition') {
                return getFileFromContentDisposition(response.responseHeaders[i].value);
            }
        }
    };

    var isHtml = function (response) {
        for (var i = 0; i < response.responseHeaders.length; i++) {
            if (response.responseHeaders[i].name.toLowerCase() == 'content-type') {
                return (response.responseHeaders[i].value.indexOf("text/html") != -1);
            }
        }
    };

    var shouldInterceptFile = function (request, response) {
        var url = response.url;
        var isAttachment = false;
        if (isBlocked(url)) {
            return false;
        }

        if (isHtml(response)) {
            return false;
        }
        var file = getAttachedFile(response);
        if (!file) {
            file = getFileFromUrl(url);
        } else {
            isAttachment = true;
        }
        var ext = getFileExtension(file);
        if (ext) {
            if (!isAttachment) {
                for (var i = 0; i < vidExts.length; i++) {
                    if (vidExts[i] == ext.toUpperCase()) {
                        return false;
                    }
                }
            }
            for (var i = 0; i < fileExts.length; i++) {
                if (fileExts[i] == ext.toUpperCase()) {
                    return true;
                }
            }
        }
    };

    var isBlocked = function (url) {
        for (var i = 0; i < blockedHosts.length; i++) {
            var hostName = parseUrl(url).hostname;
            if (blockedHosts[i] == hostName) {
                return true;
            }
        }
        return false;
    };

    var syncMDM = function () {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState == XMLHttpRequest.DONE) {
                if (xhr.status == 200) {
                    var data = JSON.parse(xhr.responseText);
                    monitoring = data.enabled;
                    blockedHosts = data.blockedHosts;
                    videoUrls = data.videoUrls;
                    fileExts = data.fileExts;
                    vidExts = data.vidExts;
                    isMDMUp = true;
                    videoList = data.vidList;
                    if (data.mimeList) {
                        mimeList = data.mimeList;
                    }
                    updateBrowserAction();
                }
                else {
                    isMDMUp = false;
                    monitoring = false;
                    updateBrowserAction();
                }
            }
        };

        xhr.open('GET', MDMHost + "/sync", true);
        xhr.send(null);
    };

    var getFileFromUrl = function (str) {
        return ustr = parseUrl(str).pathname;
    };

    var getFileFromContentDisposition = function (str) {
        var arr = str.split(";");
        for (var i = 0; i < arr.length; i++) {
            var ln = arr[i].trim();
            if (ln.indexOf("filename=") != -1) {
                log("matching line: " + ln);
                var arr2 = ln.split("=");
                log("name: " + arr2[1]);
                return arr2[1].replace(/"/g, '').trim();
            }
        }
    };

    var getFileExtension = function (file) {
        var index = file.lastIndexOf(".");
        if (index > 0) {
            return file.substr(index + 1);
        }
    };

    var parseUrl = function (str) {
        var match = str.match(/^(https?\:)\/\/(([^:\/?#]*)(?:\:([0-9]+))?)([\/]{0,1}[^?#]*)(\?[^#]*|)(#.*|)$/);
        return match && {
            href: str,
            protocol: match[1],
            host: match[2],
            hostname: match[3],
            port: match[4],
            pathname: match[5],
            search: match[6],
            hash: match[7]
        }
    };

    var removeRequest = function (requestId) {
        for (var i = 0; i < requests.length; i++) {
            if (requests[i].requestId == requestId) {
                return requests.splice(i, 1);
            }
        }
    };

    var updateBrowserAction = function () {
        if (!isMDMUp) {
            setBrowserActionPopUp("fatal.html");
            setBrowserActionIcon("app-blocked.png");
            return;
        }
        setBrowserActionPopUp(monitoring ? "status.html" : "disabled.html");
        setBrowserActionIcon(monitoring && !disabled ? "app.png" : "app-disabled.png");

        if (videoList && videoList.length > 0) {
            chrome.browserAction.setBadgeText({ text: videoList.length + "" });
        } else {
            chrome.browserAction.setBadgeText({ text: "" });
        }
    };

    var setBrowserActionIcon = function (icon) {
        if (lastIcon == icon) {
            return;
        }
        chrome.browserAction.setIcon({ path: icon });
        lastIcon = icon;
    };

    var setBrowserActionPopUp = function (pop) {
        if (lastPopup == pop) {
            return;
        }
        chrome.browserAction.setPopup({ popup: pop });
        lastPopup = pop;
    };



    var initSelf = function () {
        //This will add the request to request array for later use, 
        //the object is removed from array when request completes or fails
        chrome.webRequest.onSendHeaders.addListener
            (
            function (info) { requests.push(info); },
            { urls: ["http://*/*", "https://*/*"] },
            ["requestHeaders","extraHeaders"]
            );
        chrome.webRequest.onCompleted.addListener
            (
            function (info) {
                removeRequest(info.requestId);
            },
            { urls: ["http://*/*", "https://*/*"] }
            );

        chrome.webRequest.onErrorOccurred.addListener
            (
            function (info) {
                removeRequest(info.requestId);
            },
            { urls: ["http://*/*", "https://*/*"] }
            );

        //This will monitor and intercept files download if 
        //criteria matches and MDM is running
        //Use request array to get request headers
        chrome.webRequest.onHeadersReceived.addListener
            (
            function (response) {
                var requests = removeRequest(response.requestId);
                if (!isMDMUp) {
                    return;
                }

                if (!monitoring) {
                    return;
                }

                if (disabled) {
                    return;
                }

                if (!(response.statusLine.indexOf("200") > 0
                    || response.statusLine.indexOf("206") > 0)) {
                    return;
                }

                if (requests) {
                    if (requests.length == 1) {
                        if (!(response.url + "").startsWith(MDMHost)) {
                            //console.log("processing request " + response.url);
                            return processRequest(requests[0], response);
                        }
                    }
                }
            },
            { urls: ["http://*/*", "https://*/*"] },
            ["blocking", "responseHeaders"]
            );

        //check MDM if is running and enable monitoring
        //setInterval(function () { syncMDM(); }, 5000);

        chrome.runtime.onMessage.addListener(
            function (request, sender, sendResponse) {
                if (request.type === "links") {
                    var arr = [];
                    arr = request.links;
                    /* for (var i = 0; i < arr.length; i++) {
                        console.log("link " + arr[i]);
                    } */
                    sendUrlsToMDM(arr);
                    sendResponse({ done: "done" });
                }
                else if (request.type === "stat") {
                    var resp = { isDisabled: disabled };
                    resp.list = videoList;
                    sendResponse(resp);
                }
                else if (request.type === "cmd") {
                    disabled = request.disable;
                    log("disabled " + disabled);
                }
                else if (request.type === "vid") {
                    port.postMessage({"message":"/item\r\n"+request.itemId});

                    // var xhr = new XMLHttpRequest();
                    // xhr.open('POST', MDMHost + "/item", true);
                    // xhr.send(request.itemId);
                }
                else if (request.type === "clear") {
                    port.postMessage({"message":"/clear"});

                    // var xhr = new XMLHttpRequest();
                    // xhr.open('GET', MDMHost + "/clear", true);
                    // xhr.send();
                }
            }
        );

        chrome.commands.onCommand.addListener(function (command) {
            if (isMDMUp && monitoring) {
                log("called")
                disabled = !disabled;
            }
        });

        chrome.contextMenus.create({
            title: "Download with MDM",
            contexts: ["link", "video", "audio"],
            onclick: sendLinkToMDM,
        });

        chrome.contextMenus.create({
            title: "Download Image with MDM",
            contexts: ["image"],
            onclick: sendImageToMDM,
        });

        chrome.contextMenus.create({
            title: "Download all links",
            contexts: ["all"],
            onclick: runContentScript,
        });


        /*
        On startup, connect to the "native" app.
        */
    //    port = chrome.runtime.connectNative("com.mdm");
    connectToNativeMessagingHost().catch(err => {
        //play nice with older MDM versions
        setInterval(function () { syncMDM(); }, 5000);
    });
    function connectToNativeMessagingHost() {
        return new Promise((resolve, reject) => {
            try {
                log("Connecting to native messaging host: com.mdm");
                port = chrome.runtime.connectNative("com.mdm");
                log("Connected to native messaging host");
                port.onDisconnect.addListener(function () {
                    log("Disconnected from native messaging host!");
                    hasNativeMessagingHost = false;
                    isMDMUp = false;
                    updateBrowserAction();
                    port = undefined;
                    reject("disconnected from native host");
                });
                port.onMessage.addListener((data) => {
                    log(JSON.stringify(data));
                    if (data.appExited) {
                        postNativeMessage({});
                        isMDMUp = false;
                        hasNativeMessagingHost = false;
                    } else {
                        monitoring = data.enabled;
                        blockedHosts = data.blockedHosts;
                        videoUrls = data.videoUrls;
                        fileExts = data.fileExts;
                        vidExts = data.vidExts;
                        isMDMUp = true;
                        hasNativeMessagingHost = true;
                        videoList = data.vidList;
                        if (data.mimeList) {
                            mimeList = data.mimeList;
                        }
                        if (data.videoUrlsWithPostReq) {
                            videoUrlsWithPostReq = data.videoUrlsWithPostReq;
                        }
                    }
                    updateBrowserAction();
                    resolve(true);
                });
            } catch (err) {
                log("Error while creating native messaging host");
                log(err);
                reject("unable to connect to native host");
            }
        });
    }
       /*
       Listen for messages from the app.
       */
    //    port.onMessage.addListener((data) => {
    //                monitoring = data.enabled;
    //                blockedHosts = data.blockedHosts;
    //                videoUrls = data.videoUrls;
    //                fileExts = data.fileExts;
    //                vidExts = data.vidExts;
    //                isMDMUp = true;
    //                videoList = data.vidList;
    //                if (data.mimeList) {
    //                    mimeList = data.mimeList;
    //                }
    //                updateBrowserAction();

    //        log("Received: " + data);
    //    });

       /*
       On start up send the app a message.
       */
       log("Sending to native...")
       port.postMessage({"message":"hello from extension"});
    };

    initSelf();
    log("loaded");
})();
