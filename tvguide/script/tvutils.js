function makeNFO(progid)
{
   // alert("makeNFO: sending NFO for " + progid);
   var episode = document.getElementById(progid + "NFO").textContent;
   var plot = document.getElementById(progid + "PLT").textContent.trim();
   var postdata = '{"episode":' +  episode + ', "plot":"' + plot + '"}';
   alert("makeNFO: sending NFO data: " + postdata);
    $.post("../crit/createnfo.php", postdata,
       function(data) 
       {
         alert("NFO saved to repository.");
         // data contains anything 'echo'd by the PHP script
       });
       
       return;
}
