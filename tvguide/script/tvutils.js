function makeNFO(progid)
{
	alert("makeNFO: sennding NFO for " + progid);
   var episode = document.getElementById(progid + "NFO").textContent;
   var plot = document.getElementById(progid + "PLT").textContent.trim();
   var show = document.getElementById(progid).textContent;
   var postdata = '{"show":"' + show + '", ' + episode + ', "plot":"' + plot + '"}';
    $.post("../crit/createnfo.php", postdata,
       function(data) 
       {
         alert("makeNFO: response: " + data);
         // data contains the response which is not relevant for NFO
       });
       
       return;
}
