React = require 'react'

List = require 'material-ui/lib/lists/list'
Paper = require 'material-ui/lib/paper'

LeftView = React.createClass
  render: () ->
    console.log @props
    <div style={width: '25%'}>
      <Paper style={width: '100%', marginBottom: 10, padding: 10} zDepth={0} children={
        <div>
         <h4>{@props.name}</h4>
         <table className="table">
           <thead>
             <tr>
               <td></td>
               <td>Available</td>
               <td>In your Collection</td>
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
             </tr>
           </tbody>
         </table>
        </div>
        } />
        <List subheader={@props.name + " Artists"}>
         {@props.artists}
        </List>
      </div>


module.exports = LeftView
