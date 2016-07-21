// TODO: retrieve data by API
var clusters = [
    {
        name: 'Cluster 1',
        volumes: [{
            name: 'Volume 1.1',
            enabled: true
        },
        {
            name: 'Volume 1.2',
            enabled: false
        }]
    },
     {
        name: 'Cluster 2',
        volumes: []
    }
];

function toggleCheckbox(element)
{
    console.log(element.checked);
    // TODO: make API call to change state of volume
}


var  div = document.getElementById( 'clusters' );
// TODO: add name to checkbox to mark volume by API
function addCluster(cluster){
    cluster_id = cluster.name.replace(/\s+/g, '');
    const template = `<button type="button" class="btn btn-info cluster" data-toggle="collapse" data-target="#${cluster_id}">${cluster.name}</button>
    <div id="${cluster_id}" class="collapse">
    <ul>`
    var result = template;
    cluster.volumes.forEach(function(volume){
        result += `<li>${volume.name} <input type="checkbox" name="" onchange="toggleCheckbox(this)"`
        if (volume.enabled) {
            result +=  `checked></li>`
        } else {
            result +=  `></li>`
        }
    });
    result += `</ul>
                 </div><br/>`;

    div.insertAdjacentHTML( 'beforeend', result );
}

$(document).ready(function(){
    clusters.forEach(function(cluster){
        addCluster(cluster);
    });
    var form = document.getElementById('cluster_form'); // form has to have ID: <form id="formID">
    form.noValidate = true;
    form.addEventListener('submit', function(event) { // listen for form submitting
            if (!event.target.checkValidity()) {
                event.preventDefault(); // dismiss the default functionality
                alert('Please, fill the cluster name'); // error message
            }
        }, false);
});

// TODO: send cluster_name to endpoint
$("#cluster_form").submit(function(e) {
    e.preventDefault(); // avoid to execute the actual submit of the form.

    var url = "path/to/your/api"; // the script where you handle the form input.
    console.log($("#cluster_form").serialize());
    $.ajax({
           type: "POST",
           url: url,
           data: $("#cluster_form").serialize(), // serializes the form's elements.
           success: function(data)
           {
               alert(data); // show response from the php script.
           }
         });

    
});

