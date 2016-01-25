import React from 'react';
import ReactDOM from 'react-dom';
import injectTapEventPlugin from 'react-tap-event-plugin';
import Visualization from  './visualize/Charts';

injectTapEventPlugin();

ReactDOM.render(<Visualization />, document.getElementById('content'));
