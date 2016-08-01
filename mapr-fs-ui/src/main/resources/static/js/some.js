function toggleCheckbox(cluster_name, volume_name) {
    return function(element) {
        console.log(cluster_name, volume_name)
        console.log(element.target.checked);

        function ClusterData(cluster_name, volume_name, replication) {
            this.cluster_name = cluster_name;
            this.volume_name = volume_name;
            this.replication = replication;
        }

        var cluster = new ClusterData(cluster_name, volume_name, element.target.checked);



       var url = "http://centos7-sn:8080/v1/volumes/status"; // the script where you handle the form input.
            console.log($(this).serialize());
            var self = this;
            $.ajax({
                   type: "POST",
                   url: url,
                   data: cluster, // serializes the form's elements.
                   success: function(data)
                   {
                       console.log("success"); // show response from the php script.
                   }
                 });
    }
    // TODO: make API call to change state of volume
}

var  div = document.getElementById( 'clusters' );
// TODO: add name to checkbox to mark volume by API
function addCluster(cluster){
    cluster_id = cluster.cluster_name.replace(/\s+/g, '');
    const template = `<button type="button" class="btn btn-info cluster" data-toggle="collapse" data-target="#${cluster_id}">${cluster.name}</button>
    <div id="${cluster_id}" class="collapse">
    <ul>`
    var result = template;
    cluster.volumes.forEach(function(volume){
        result += `<li>${volume.name} <input type="checkbox" name="" id="${cluster_id}_${volume.name}"`
        if (volume.enabled) {
            result +=  `checked></li>`
        } else {
            result +=  `></li>`
        }

    });
    result += `</ul>
        <div id='control-${cluster_id}'>
            <form id="cluster_${cluster_id}_form">
              <input type="hidden" name="cluster_name" value="${cluster.cluster_name}">
              <input type="text" name="volume_name" placeholder="Add volume name here" required = "true">
              <input type="submit" value="Add">
            </form>
        </div>
     </div><br/>`;
    div.insertAdjacentHTML( 'beforeend', result );
     var form = document.getElementById(`cluster_${cluster_id}_form`); // form has to have ID: <form id="formID">
     form.noValidate = true;
     form.addEventListener('submit', function(event) { // listen for form submitting
             if (!event.target.checkValidity()) {
                 event.preventDefault(); // dismiss the default functionality
                 alert('Please, fill the cluster name'); // error message
             }
         }, false);

    cluster.volumes.forEach(function(volume){
        var func  = toggleCheckbox(cluster.cluster_name, volume.name)
        $(`#${cluster_id}_${volume.name}`).change(function(e){
            func(e)
        })
     });

    $(`#cluster_${cluster_id}_form`).submit(function(e) {
        e.preventDefault();

        var url = "http://centos7-sn:8080/v1/volumes"; // the script where you handle the form input.
        console.log($(this).serialize());
        var self = this;
        $.ajax({
               type: "POST",
               url: url,
               data: $(this).serialize(), // serializes the form's elements.
               success: function(data)
               {
                   console.log("volume added"); // show response from the php script.
                   $(self)[0].reset();
               }
             });
    });
}

//function mapClusters(clusters)
function reloadClusters() {
    div.innerHTML = "";
    $.ajax({
            url: "http://centos7-sn:8080/v1/clusters/",
//            crossDomain: true,
//            headers: {
//                'Access-Control-Allow-Headers': '*'
//            },
            success: function (response) {
                response.clusters.map(function(cluster) {
                    cluster.name = cluster.cluster_name;
                    cluster.volumes = cluster.volumes.filter(function(volume) {
                        //TODO refactor server which return null in volumes
                        return volume;
                    }).map(function(volume) {
                        volume.enabled = volume.replicating;
                        return volume;
                    });
                    return cluster;
                }).forEach(function(cluster){
                    addCluster(cluster);
                });
            }
        });

}



$(document).ready(function(){
    reloadClusters()
    setInterval(reloadClusters, 10*1000)
    var form = document.getElementById('cluster_form'); // form has to have ID: <form id="formID">
    form.noValidate = true;
    form.addEventListener('submit', function(event) { // listen for form submitting
            if (!event.target.checkValidity()) {
                event.preventDefault(); // dismiss the default functionality
                alert('Please, fill the cluster name'); // error message
            }
        }, false);
});


$("#cluster_form").submit(function(e) {
    e.preventDefault();

    var url = "http://centos7-sn:8080/v1/clusters"; // the script where you handle the form input.
    console.log($("#cluster_form").serialize());
    $.ajax({
           type: "POST",
           url: url,
           data: $("#cluster_form").serialize(), // serializes the form's elements.
           success: function(data)
           {
               console.log("cluster added"); // show response from the php script.
               $("#cluster_form")[0].reset();
           }
         });
});


