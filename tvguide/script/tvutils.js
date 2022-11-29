function makeNFO(progid)
{
   // alert("makeNFO: sending NFO for " + progid);
   var episode = document.getElementById(progid + "NFO").textContent;
   var plot = document.getElementById(progid + "PLT").textContent.trim();
   var postdata = '{"episode":' +  episode + ', "plot":"' + plot + '"}';
   // console.log("makeNFO: sending NFO data: " + postdata);
    $.post("../crit/createnfo.php", postdata,
       function(data) 
       {
         console.log("Response: " + data);
         
         // Should only display anything if not successful
         if(!data.startsWith("SUCCESS:"))
         {
            alert(data);
         }
       });
       
       return;
}

function makeNFOquiet(progid)
{
   // alert("makeNFO: sending NFO for " + progid);
   var episode = document.getElementById(progid + "NFO").textContent;
   var plot = document.getElementById(progid + "PLT").textContent.trim();
   var postdata = '{"episode":' +  episode + ', "plot":"' + plot + '"}';
   $.post("../crit/createnfo.php", postdata,
       function(data) 
       {
         console.log("Response: " + data);
       });
       
       return;
}
