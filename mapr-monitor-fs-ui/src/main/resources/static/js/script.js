var val = '';
var API_ENDPOINT = 'http://centos7-sn:3000/v1/'
$.ajax({
  crossDomain: true
});

$(document).ready(function(){
    retrieve_data();
});
function changeStatus(name, monitoring, _id){
    $.post(`${API_ENDPOINT}/volumes`, {volume_name: name, path: _id, monitoring: !monitoring});
    window.location.reload();
}
function render_table(table_data){
    $('#table_body').empty();
    var rows = '';
    table_data.forEach(function(file){
        rows += `<tr>
        <td>${file.name}</td>
        <td>${file._id}</td>
        <td><input type="checkbox" name="" id="${file.name}"`
        if (file.monitoring) {
              rows +=  `checked></td></tr>`
            } else {
              rows +=  `></td></tr>`
            }
    });
    $('#table_body' ).html(rows);
    table_data.forEach(function(file){
            $(`#${file.name}`).click(function() {
                changeStatus(file.name, file.monitoring, file._id);
            });
        });
}


function retrieve_data() {

    $.get(`${API_ENDPOINT}/volumes`, function( data ) {
        console.log(data);
        render_table(data);

    });
    setTimeout(retrieve_data, 2*1000)
}



$(`#volume_form`).on('submit', function(event) {
    handler_click(this[0].value, this[1].value);
    console.log("Success!")
});


function handler_click(volume_name, path) {
    $.post( `${API_ENDPOINT}/volumes`, { volume_name: volume_name, path: path, monitoring: true } );
}