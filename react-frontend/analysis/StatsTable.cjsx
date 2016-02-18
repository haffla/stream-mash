React = require 'react'

StatsTable = React.createClass

Colors = require 'material-ui/lib/styles/colors'

StatsTable = React.createClass

  render: () ->
    <table className="table">
      <thead>
        <tr>
          <td></td>
          <td>Available</td>
          <td>You Have</td>
          <td>Missing</td>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>Artists</td>
          <td>{@props.nrArtists}</td>
          <td></td>
          <td>
            <span style={textDecoration: 'underline', color: Colors.blue500, cursor: 'pointer'}
                  onClick={@props.openMissingItemsDialog.bind(null, 'artist')}>
              {@props.nrAbsentArtist}
            </span>
          </td>
        </tr>
        <tr>
          <td>Albums</td>
          <td>{@props.nrAlbumsTotal}</td>
          <td>{@props.nrAlbumsInUserCollection}</td>
          <td>
           <span style={textDecoration: 'underline', color: Colors.blue500, cursor: 'pointer'}
                 onClick={@props.openMissingItemsDialog.bind(null, 'album')}>
             {@props.nrMissingAlbums}
           </span>
          </td>
        </tr>
      </tbody>
    </table>

module.exports = StatsTable
