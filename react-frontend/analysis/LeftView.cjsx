React = require 'react'

Colors = require 'material-ui/lib/styles/colors'
List = require 'material-ui/lib/lists/list'
Paper = require 'material-ui/lib/paper'

LeftView = React.createClass
  render: () ->
    children =
      <div>
       <h4>{@props.name}</h4>
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
           </tr>
           <tr>
             <td>Albums</td>
             <td>{@props.nrAlbumsTotal}</td>
             <td>{@props.nrAlbumsInUserCollection}</td>
             <td>
              <span style={textDecoration: 'underline', color: Colors.blue500, cursor: 'pointer'}
                    onClick={@props.openMissingAlbumsDialog}>
                {@props.albumsOnlyInUserCollection.length}
              </span>
             </td>
           </tr>
         </tbody>
       </table>
      </div>
    <div style={width: '25%'}>
      <Paper style={width: '100%', marginBottom: 10, padding: 10} zDepth={0} children={children} />
        <List subheader={@props.name + " Artists"}>
         {@props.artists}
        </List>
      </div>


module.exports = LeftView
