import React from 'react';
import ReactDOM from 'react-dom';
import injectTapEventPlugin from 'react-tap-event-plugin';
import AnalysisBox from './analysis/AnalysisBox';

injectTapEventPlugin();

ReactDOM.render(<AnalysisBox />, document.getElementById('content'));
