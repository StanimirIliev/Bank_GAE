import React from 'react'
import {Link} from 'react-router-dom'
import './CloseButton.css'

const CloseButton = ({to, name}) => {
    return (<Link to="/" className="linkButton button--close">Close</Link>)
}
export default CloseButton