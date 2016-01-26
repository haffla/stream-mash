import React from 'react';
import ReactDOM from 'react-dom';
import ChartsBox from './visualize/ChartsBox.cjsx';
import injectTapEventPlugin from 'react-tap-event-plugin';

injectTapEventPlugin();

ReactDOM.render(<ChartsBox />, document.getElementById('content'));
