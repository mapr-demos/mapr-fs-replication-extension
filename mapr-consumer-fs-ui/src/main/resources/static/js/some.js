var API_ENDPOINT = ''


function addCluster(cluster) {
    cluster_id = cluster.cluster_name.replace(/\s+/g, '');
    var template = null;
    if (cluster.state) {
        template = `<button type="button" class="btn btn-info cluster" data-toggle="collapse" data-target="#${cluster_id}"  id="${cluster_id}_button">${cluster.name}</button>
    <div id="${cluster_id}" class="collapse in">
    `
    } else {
        template = `<button type="button" class="btn btn-info cluster" data-toggle="collapse" data-target="#${cluster_id}"  id="${cluster_id}_button">${cluster.name}</button>
    <div id="${cluster_id}" class="collapse">
    `

    }
    var result = template;
    result += `
    <div class="cluster_volumes">
      <div id='${cluster_id}_list'></div>
          <div id='control-${cluster_id}' aria-expanded='${cluster.state}' >
              <form id="cluster_${cluster_id}_form" class="cluster_input_form">
                  <input type="hidden" name="cluster_name" value="${cluster.cluster_name}">
                  <div id="volume_name_input">
                    <input class="form-control" type="text" name="volume_name" placeholder="Add volume name here" required = "True">
                  </div>
                  <div id="volume_path_input">
                    <input class="form-control" type="text" name="path" width="50%" placeholder="Add volume path here" required = "True">
                  </div>
                  <div id="add_btn">
                    <input class="btn btn-primary" type="submit" value="Add">
                  </div>
              </form>
          </div>
      </div>
  </div><br/>`;

    var div = document.getElementById('clusters');
    div.insertAdjacentHTML('beforeend', result);


    addVolumes(cluster);

    function handler_click(cluster_id, cluster) {
        return function () {
            cluster.state = !$(`#${cluster_id}`).hasClass('in');
        }
    }

    var func = handler_click(cluster_id, cluster);


    $(`#${cluster_id}_button`).on('click', function (event) {
        func();
    });


    validateForm(`cluster_${cluster_id}_form`, 'Please, fill the volume name');


    $(`#cluster_${cluster_id}_form`).submit(function (e) {
        e.preventDefault();

        var url = `${API_ENDPOINT}/volumes`; // the script where you handle the form input.
        console.log($(this).serialize());
        var self = this;
        $.ajax({
            type: "POST",
            url: url,
            data: $(this).serialize(), // serializes the form's elements.
            statusCode: {
                403: function (response) {
                    alert('Cannot replicate to this folder!');
                }
            },
            success: function (data) {
                console.log("volume added"); // show response from the php script.
                $(self)[0].reset();
            }
        });
    });
}


function addVolumes(cluster) {
    cluster_id = cluster.cluster_name.replace(/\s+/g, '');
    var result = '<ul>';
    cluster.volumes.forEach(function (volume) {
        result += `<li>${volume.name} replicating to => ${volume.path} | Status: <input type="checkbox" name="" id="${cluster_id}_${volume.name}"`
        if (volume.replicating) {
            result += `checked></li>`
        } else {
            result += `></li>`
        }

    });
    result += '</ul>';
    var div = document.getElementById(`${cluster_id}_list`);
    div.innerHTML = '';
    div.insertAdjacentHTML('beforeend', result);


    cluster.volumes.forEach(function (volume) {
        var func = toggleCheckbox(cluster.cluster_name, volume.name, volume.path)
        $(`#${cluster_id}_${volume.name}`).change(function (e) {
            func(e)
        })
    });
}

var clusters = [];


function merge(clusters, response_clusters) {
    response_clusters.forEach(function (cluster) {
        existing_cluster = clusters.find(function (inner_cluster) {
            return inner_cluster.name ==
                cluster.name
        });
        if (existing_cluster) {
            existing_cluster.volumes = cluster.volumes;
        } else {
            cluster.state = false;
            addCluster(cluster);
            clusters.push(cluster);
        }
    })
}

function reloadClusters() {
    $.ajax({
        url: `${API_ENDPOINT}/clusters`,
        crossDomain: true,
        header: 'Access-Control-Allow-Origin: *',
        success: function (response) {
//      response = JSON.parse( response)
            response.clusters.map(function (cluster) {
                cluster.name = cluster.cluster_name;
                cluster.volumes = cluster.volumes.filter(function (volume) {
                    //TODO refactor server which return null in volumes
                    return volume;
                });
                return cluster;
            });
            merge(clusters, response.clusters);
            clusters.forEach(function (cluster) {
                addVolumes(cluster);
            });
        }
    });
}


$(document).ready(function () {
    reloadClusters()
    setInterval(reloadClusters, 10 * 1000)
    var form = document.getElementById('cluster_form'); // form has to have ID: <form id="formID">
    form.noValidate = true;
    form.addEventListener('submit', function (event) { // listen for form submitting
        if (!event.target.checkValidity()) {
            event.preventDefault(); // dismiss the default functionality
            alert('Please, fill the cluster name'); // error message
        }
    }, false);
    validateForm('cluster_form', 'Please, fill the cluster name')
});

function validateForm(form_id, message) {
    var form = document.getElementById(form_id); // form has to have ID: <form id="formID">
    form.noValidate = true;
    form.addEventListener('submit', function (event) { // listen for form submitting
        if (!event.target.checkValidity()) {
            event.preventDefault(); // dismiss the default functionality
            alert(message); // error message
        }
    }, false);
}


$("#cluster_form").submit(function (e) {
    e.preventDefault();

    var url = `${API_ENDPOINT}/clusters`; // the script where you handle the form input.
    console.log($("#cluster_form").serialize());
    $.ajax({
        type: "POST",
        url: url,
        data: $("#cluster_form").serialize(), // serializes the form's elements.
        success: function (data) {
            console.log("cluster added"); // show response from the php script.
            $("#cluster_form")[0].reset();
        }
    });
});


function toggleCheckbox(cluster_name, volume_name, path) {
    return function (element) {
        console.log(cluster_name, volume_name, path)
        console.log(element.target.checked);

        function ClusterData(cluster_name, volume_name, path, replication) {
            this.cluster_name = cluster_name;
            this.volume_name = volume_name;
            this.replication = replication;
            this.path = path;
        }

        var cluster = new ClusterData(cluster_name, volume_name, path, element.target.checked);


        var url = `${API_ENDPOINT}/volumes/status`; // the script where you handle the form input.
        console.log($(this).serialize());
        var self = this;
        $.ajax({
            type: "POST",
            url: url,
            data: cluster,
            success: function (data) {
                console.log("success");
            }
        });
    }
}