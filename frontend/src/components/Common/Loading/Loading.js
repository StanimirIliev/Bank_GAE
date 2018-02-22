import React from 'react'
import './Loading.css'

const Loading = () => (
    <div className="container__loading"  >
        <img className="loading__img"  src={require("../../../icons/loading.png")}/>
    </div>
)

export default Loading