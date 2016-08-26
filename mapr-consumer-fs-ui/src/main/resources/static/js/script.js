var val = '';


$(document).ready(function(){
    $('.selectpicker').on('changed.bs.select', function (e) {
        val = $('.selectpicker').selectpicker('val');
        retrieve_data()
    });
    retrieve_volumes();
});

function render_table(table_data){
    $('#table_body').empty();
    var rows = '';
    table_data.reverse().forEach(function(file){
        rows += `<tr><td>${file.dateTime}</td><td>${file.lastEvent}</td><td>${file.filename}</td></tr>`
    });
    $('#table_body' ).html(rows);
}


function retrieve_data() {

    $.get( `volumes/status/${val}`, function( data ) {
//        data = JSON.parse(data);
        console.log(data);
        render_table(data.files);

    });
    setTimeout(retrieve_data, 2*1000)
}

function retrieve_volumes() {
    $('.selectpicker').empty();
    $.get( `volumes/status`, function( data ) {
//        data = JSON.parse(data);
        data.forEach(function(option){
            $('.selectpicker')
                .append($("<option></option>")
                    .attr("value",option)
                    .text(option));
        });

        $('.selectpicker').selectpicker('refresh');
        $('.selectpicker').selectpicker('val', val);

    });
    setTimeout(retrieve_volumes, 2*1000)
}
