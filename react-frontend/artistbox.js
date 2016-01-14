import React from 'react';
import ReactDOM from 'react-dom';
import ArtistBox from './user_collection/ArtistBox.cjsx';
import injectTapEventPlugin from 'react-tap-event-plugin';

injectTapEventPlugin();

ReactDOM.render(<ArtistBox />, document.getElementById('content'));
