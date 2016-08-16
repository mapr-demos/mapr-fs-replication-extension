var val = '';


$(document).ready(function(){
    $('.selectpicker').on('changed.bs.select', function (e) {
        val = $('.selectpicker').selectpicker('val');
        retrive_data()
    });
    retrive_volumes();
});

function render_table(table_data){
    $('#table_body').empty();
    var rows = '';
    table_data.reverse().forEach(function(file){
        rows += `<tr><td>${file.dateTime}</td><td>${file.lastEvent}</td><td>${file.filename}</td></tr>`
    });
    $('#table_body' ).html(rows);
}


function retrive_data() {

    $.get( `volumes/status/${val}`, function( data ) {
//        data = JSON.parse(data);
        console.log(data);
        render_table(data.files);

    });
    setTimeout(retrive_data, 2*1000)
}

function retrive_volumes() {
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
    setTimeout(retrive_volumes, 2*1000)
}
