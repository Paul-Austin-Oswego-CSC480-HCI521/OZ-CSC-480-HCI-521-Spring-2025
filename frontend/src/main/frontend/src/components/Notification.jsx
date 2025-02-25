import React from "react";

const Notification = () => {
  return (
    <div className="container">
      <div className="row">
        {/* Left Section */}
{/*         <div className="col-lg-3 left"> */}
{/*           <div className="box shadow-sm mb-3 rounded bg-white text-center"> */}
{/*             <img */}
{/*               src="https://bootdey.com/img/Content/avatar/avatar7.png" */}
{/*               className="img-fluid" */}
{/*               alt="User" */}
{/*             /> */}
{/*             <div className="p-3 border-bottom"> */}
{/*               <h6 className="font-weight-bold text-dark">Notifications</h6> */}
{/*               <p className="mb-0 text-muted"> */}
{/*                 You’re all caught up! Check back later for new notifications */}
{/*               </p> */}
{/*             </div> */}
{/*             <div className="p-3"> */}
{/*               <button className="btn btn-outline-success btn-sm"> */}
{/*                 View settings */}
{/*               </button> */}
{/*             </div> */}
{/*           </div> */}
{/*         </div> */}

        {/* Right Section */}
        <div className="">
          <div className="box shadow-sm rounded bg-white mb-3">
            <div className="box-title border-bottom p-3">
              <h6 className="m-0">Recent</h6>
            </div>
            <div className="box-body p-0">
              <NotificationItem
                avatar="https://bootdey.com/img/Content/avatar/avatar3.png"
                title="Classes Remote: Monday"
                description="All classes on Mon Feb17 are remote."
                time="3d"
              />
              <NotificationItem
                avatar="https://bootdey.com/img/Content/avatar/avatar1.png"
                title="Notification"
                description="This is notifications component"
                time="4d"
                actionText="View more Notifications"
              />
            </div>
          </div>

          <div className="box shadow-sm rounded bg-white mb-3">
            <div className="box-title border-bottom p-3">
              <h6 className="m-0">Earlier</h6>
            </div>
            <div className="box-body p-0">
              <NotificationItem
                avatar="https://bootdey.com/img/Content/avatar/avatar2.png"
                title="Quotes"
                description="Quotes is inviting you to write a quote"
                time="4d"
                actionText="Say Sure!"
              />
              <NotificationItem
                avatar="https://bootdey.com/img/Content/avatar/avatar4.png"
                title="React and Openliberty"
                description="Harry Potter decided that React and Openliberty will be used"
                time="4d"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Notification Item Component
const NotificationItem = ({ avatar, title, description, time, actionText }) => {
  return (
  <div>
    <div className="d-flex align-self-center m-2 gap-3 border-bottom">
      <div className="m">
      <img className="rounded-circle ml-5" src={avatar} height='50' width='50' alt="Generic placeholder image"/>
      </div>
      <div className="media-body text-start">
        <h5 className="mt-0">{title}</h5>
        <p>{description}</p>
        <p className="mb-0">{actionText}</p>
      </div>
    </div>
  </div>
  );
};

export default Notification;
