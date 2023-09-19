var xmlHttp;// global instance of XMLHttpRequest
function createXmlHttpRequest()
{
    xmlHttp=new XMLHttpRequest();
}

function start ()
{
    var input = document.getElementById("msg");
    input.addEventListener('input', update);
    startRequest();
}

function startRequest()
{
    createXmlHttpRequest();

    xmlHttp.open("GET","/AsyncChat-1.0-SNAPSHOT/asyncchat" ,true);
    xmlHttp.onreadystatechange=handleStateChange;
    xmlHttp.send(null);
}

function handleStateChange()
{
    if (xmlHttp.readyState===4)
    {
        if(xmlHttp.status===200)
        {
            console.log(xmlHttp.responseText);

            var message = xmlHttp.responseText;
            if(message !== "") {
                document.getElementById("msgs").innerHTML = message;
                startRequest();
            }
        }
        else if (xmlHttp.status !== 0)  // the pending request of a page that we already left
        {
            alert("Error loading page "+ xmlHttp.status + ":"+xmlHttp.statusText);
        }
    }
}

function update(e)
{
    var xmlHttp2;
    if (window.ActiveXObject)
    {
        xmlHttp2=new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if (window.XMLHttpRequest)
    {
        xmlHttp2=new XMLHttpRequest();
    }
    var name = window.localStorage.getItem('nickname');

    var parameters = "msg=" + e.target.value;
    xmlHttp2.open("POST", "chatservlet", true);
    xmlHttp2.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xmlHttp2.send(parameters);
    console.log(xmlHttp2);
    console.log("sending " + parameters + "&x=B");
}

function mySubmit()
{
    var form = document.getElementById("nameForm");
    var name = document.getElementById("nickname");
    window.localStorage.setItem('nickname', name.value);
    form.submit();
}