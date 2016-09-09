var API_URL = ''
var sourceID = 0;
function fetchSources() {
    $('#add_new_src_btn').off('click');
    fetch(API_URL + '/sources').then(function(response) {
        $('#add_new_src_btn').on('click', addNewSourceHandler);
        return response.json();
    }).then(function(data) {
        sourceID = 0;
        var parent = document.getElementById('source-list');
        removeAllChild(parent);
        var result = [];

        data.sources.forEach(function(rawSource) {
            rawSource.volumes
                .filter(function(volume) {
                  //TODO refactor server which return null in volumes
                  return volume;
                })
                .reverse()
                .forEach(function(volume) {
                    result.push({
                        sourceID: sourceID++,
                        bucketName: rawSource._id,
                        volumeName: volume.volumeName,
                        path: volume.path,
                        creating: volume.creating,
                        deleting: volume.deleting,
                        moving: volume.moving,
                        modifying: volume.modifying
                    });
                });
        });

        result.forEach(function(source) {
            popElement(parent, createSourceForm(parent, source));
        });
    });
}

function addNewSourceHandler() {
    var parent = document.getElementById('source-list');
    var source = {
        sourceID: sourceID++,
        bucketName: '',
        path: '',
        creating: true,
        modifying: true,
        moving: true,
        deleting: true,
        volumeName: ''
    };
    popElement(parent, createSourceForm(parent, source));
}

function createElement(type, props, child) {
    var elem = document.createElement(type);
    for (var prop in props) {
        if (prop === 'style') {
            var style = props[prop];
            for (var styleProp in style) {
                elem.style[styleProp] = style[styleProp];
            }
            continue;
        }
        elem[prop] = props[prop];
    }
    var _child = child || [];
    _child.forEach(function(children) {
        elem.appendChild(children);
    });
    return elem;
}
$(document).ready(function(){
    fetchSources();
});

function removeAllChild(elem) {
    while(elem.firstChild) {
        elem.removeChild(elem.firstChild);
    }
}

function popElement(parent, elem) {
    parent.insertBefore(elem, parent.firstChild);
}

function createSourceForm(parent, source) {
    var id = 'source_form_' + source.sourceID;
    var isNewSource = !source.volumeName;
    var child = [];

// --------------------- Text Inputs ---------------------

    child.push(createElement('div',
                    {className: 'form_element'},
                    [createFormLabel('Volume: '), createVolumeInput(source)]));
    child.push(createElement('div',
                    {className: 'form_element'},
                    [createFormLabel('Bucket: '), createBucketInput(source)]));
    child.push(createElement('div',
                        {className: 'form_element'},
                        [createFormLabel('Path: '), createPathInput(source)]));


// --------------------- Checkboxes ---------------------


    child.push(createElement('div',
                    {className: 'form_element form_checkbox'},
                    [createCheckbox(source, 'creating', isNewSource), createFormLabel('Create File')]));
    child.push(createElement('div',
                    {className: 'form_element form_checkbox'},
                    [createCheckbox(source, 'deleting', isNewSource), createFormLabel('Delete File')]));
    child.push(createElement('div',
                    {className: 'form_element form_checkbox'},
                    [createCheckbox(source, 'modifying', isNewSource), createFormLabel('Modify File')]));
    child.push(createElement('div',
                    {className: 'form_element form_checkbox'},
                    [createCheckbox(source, 'moving', isNewSource), createFormLabel('Rename File')]));

// --------------------- Buttons ---------------------

    if (!!source.bucketName && !!source.volumeName && !!source.path) {

        child.push(createElement('div',
                        {className: 'btn btn-primary',
                        style: {
                            marginBottom: '2px',
                        },
                        onclick: function () {
                                         deleteSource(source);
                                         parent.removeChild(document.getElementById(id));
                         }},
                        [document.createTextNode('Remove')]));
    } else {

        child.push(createElement('div',
                        {className: 'btn btn-primary',
                        style: {
                            marginBottom: '2px',
                        },
                        onclick: function () {
                                         if (source.volumeName && source.bucketName) {
                                             sendData(source);
                                             window.location.reload();
                                         } else {
                                            alert('Enter bucket, volume and path !');
                                         }
                         }},
                        [document.createTextNode('Add new Source')]));
    }

    return createElement('div', {
        id: id,
        style: {
            border: 'thick solid #87CEFA',
            padding: '30px',
            width: '400px',
            align: 'center'
        }
    }, child);
}


function deleteSource(source) {
    var volume_name = document.getElementById('volume_input_' + source.sourceID).value;
    var bucket_name = document.getElementById('bucket_input_' + source.sourceID).value;
    var path_name = document.getElementById('path_input_' + source.sourceID).value;

    $.post(`${API_URL}/sources/del`, {
        volume_name: volume_name,
        bucket: bucket_name,
        path: path_name
    });
}

function sendData(source) {
    console.log(source);
    if (source.volumeName && source.bucketName && source.path) {
        $.post(`${API_URL}/sources`, {
            volume_name: source.volumeName,
            bucket: source.bucketName,
            path: source.path,
            creating: source.creating,
            deleting: source.deleting,
            modifying: source.modifying,
            moving: source.moving
        });
    }
}

function createVolumeInput(source) {
    var props = {
        id: 'volume_input_' + source.sourceID,
        value: source.volumeName,
        readOnly: !!source.volumeName,
        placeholder: 'Enter Volume Name',
        onchange: function(e) {
            source.volumeName = e.target.value;
        }
    };
    return createElement('input', props);
}

function createBucketInput(source) {
    var props = {
        id:'bucket_input_' + source.sourceID,
        value: source.bucketName,
        readOnly: !!source.bucketName,
        placeholder: 'Enter Bucket Name',
        onchange: function(e) {
            source.bucketName = e.target.value;
        }
    };
    return createElement('input', props);
}

function createPathInput(source) {
    var props = {
        id:'path_input_' + source.sourceID,
        value: source.path,
        readOnly: !!source.path,
        placeholder: 'Enter Path',
        onchange: function(e) {
            source.path = e.target.value;
        }
    };
    return createElement('input', props);
}

function checkBoxHandlerFactory(source, fieldName, isNewSource) {
    return function() {
        source[fieldName] = !source[fieldName];
        if (!isNewSource) {
            sendData(source);
        }
    };
}
function createCheckbox(source, fieldName, isNewSource) {
    var value = source[fieldName];
    return createElement('input', {
        type: 'checkbox',
        name: fieldName,
        id: fieldName + '_checkbox_' + source.sourceID,
        checked: value,
        onchange: checkBoxHandlerFactory(source, fieldName, isNewSource),
        value: value
    })
}

function createFormLabel(text) {
    return createElement('div', {
        className: 'form_label'
    }, [document.createTextNode(text)]);
}
