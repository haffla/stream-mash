var MainComponent = React.createClass({
    getInitialState: function() {
        return {data: []};
    },
    componentDidMount: function() {
        this.setState({data: []});
    },
    preventDef: function (event) {
        event.stopPropagation();
        event.preventDefault();
        $('#dropzone').addClass('hover');
    },
    dragEnter: function (event) {
        this.preventDef(event);
    },
    dragLeave: function (event) {
        this.preventDef(event);
        $('#dropzone').removeClass('hover')
    },
    drop: function (event) {
        this.preventDef(event);
        if(!(window.File && window.FileReader)) {
            return alert("Your browser does not support the File API");
        }
        var file = event.dataTransfer.files[0];
        if(file.type === 'text/xml' && file.name.match(/^iTunes (Music )?Library/)) {
            var formData = new FormData();
            formData.append('file', file);
            $('#dropzone').addClass('dropped');
            $.ajax({
                url: '/itunes',
                dataType: 'json',
                type: 'POST',
                data: formData,
                cache: false,
                contentType: false,
                processData: false,
                success: function(data) {
                    var formattedData = Object.keys(data).map(function(key) {
                        var albums = data[key].map(function(name) {
                            return {name: name};
                        });
                        return {name: key, albums: albums};
                    });
                    $('#artistList').removeClass('hidden');
                    $('#dropzone').removeClass('dropped hover');
                    this.setState({data: formattedData});
                }.bind(this),
                error: function(xhr, status, err) {
                    console.error(this.props.url, status, err.toString());
                }.bind(this)
            });
        } else {
            return alert("Nono!! Only XML");
        }

    },
    render: function() {
        return (
        <div className="container">
            <div title="Drop your iTunes Library file here" id="dropzone" onDragOver={this.preventDef}
                 onDrop={this.drop} onDragEnter={this.dragEnter} onDragLeave={this.dragLeave}>
                <p>iTunes Library</p>
            </div>
            <ArtistBox data={this.state.data} />
        </div>
        )
    }
});

var ArtistBox = React.createClass({

    render: function() {
        return (
            <div className="hidden" id="artistList">
                <h2>Listing Artists</h2>
                <ArtistList data={this.props.data} />
            </div>
        );
    }
});

var ArtistList = React.createClass({
    render: function () {
        var artists = this.props.data.map(function(artist) {
            return (
                <div className="artist">
                    <h3>{artist.name}</h3>
                    <Artist albums={artist.albums}/>
                </div>
            );
        });
        return (
            <div className="artistList">
                {artists}
            </div>
        );
    }
});

var Artist = React.createClass({
    render: function() {
        return (
            <div className="albumList">
                <AlbumList albums={this.props.albums}/>
            </div>
        );
    }
});

var AlbumList = React.createClass({
    render: function() {
        var albums = this.props.albums.map(function(album) {
            return (
                <Album name={album.name} />
            );
        });
        return (
            <div className="albumList">
                {albums}
            </div>
        )
    }
});

var Album = React.createClass({
    render: function() {
        return (
            <div className="album">
                <span>{this.props.name}</span>
            </div>
        )
    }
});

React.render(
    <MainComponent/>,
    document.getElementById('content')
);