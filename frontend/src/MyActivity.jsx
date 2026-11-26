import { useState, useEffect } from 'react';
import { IndianRupee, CheckCircle2, Heart } from 'lucide-react';
import api from './api';

export default function MyActivity() {
  const [pledges, setPledges] = useState([
    { id: 1, campaign: "Kolhapur Flood Relief", detail: "₹1,000 donation", status: "RECEIVED" },
    { id: 2, campaign: "Kolhapur Flood Relief", detail: "25 kg Rice", status: "PLEDGED" },
    { id: 3, campaign: "Panhala Shelter Camp", detail: "₹500 donation", status: "PLEDGED" }
  ]);

  const [tasks, setTasks] = useState([
    { id: 1, campaign: "Kolhapur Flood Relief", title: "Distribute food packets", details: "Report to Ward 4 school at 8 AM. Bring ID card.", status: "ASSIGNED" },
    { id: 2, campaign: "Panhala Shelter Camp", title: "Shelter Setup", details: "", status: "APPLIED" },
    { id: 3, campaign: "Ravaneshwar Medical Camp", title: "Medical Assistance", details: "", status: "DONE" }
  ]);

  useEffect(() => {
    // Fetch live donations made by current user from Spring Boot
    api.get('/donations/my')
      .then(res => {
        const list = res.content || (Array.isArray(res) ? res : []);
        if (list.length > 0) {
          setPledges(list.map(d => ({
            id: d.id,
            campaign: d.campaignTitle || "Disaster Relief Campaign",
            detail: `₹${Number(d.amount).toLocaleString()} donation`,
            status: d.status === 'SUCCESS' ? 'RECEIVED' : d.status
          })));
        }
      })
      .catch(() => {});

    // Fetch user volunteer assignments
    api.get('/volunteer/assignments/my')
      .then(res => {
        const list = Array.isArray(res) ? res : res.content || [];
        if (list.length > 0) {
          setTasks(list.map(a => ({
            id: a.id,
            campaign: a.campaignTitle || "Relief Mission",
            title: a.roleTitle || a.requirementTitle || "Relief Task",
            details: a.location ? `Location: ${a.location}` : "Active duty",
            status: a.status || "ASSIGNED"
          })));
        }
      })
      .catch(() => {});
  }, []);

  const handleMarkDone = (id) => {
    setTasks(tasks.map(task => 
      task.id === id ? { ...task, status: "DONE" } : task
    ));
  };

  const tasksDone = tasks.filter(t => t.status === 'DONE').length;
  const totalMoney = pledges
    .filter(p => p.detail.includes('₹'))
    .reduce((sum, p) => {
      const match = p.detail.match(/₹([0-9,]+)/);
      return sum + (match ? parseInt(match[1].replace(/,/g, '')) : 0);
    }, 0);

  return (
    <div>
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 border-l-4 border-blue-600 pl-4">My Activity</h1>
        <p className="text-gray-500 mt-2 pl-4">Your kindness, tracked from pledge to proof.</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-[#E0E7FF] rounded-xl p-6 border border-blue-100">
          <p className="text-blue-900 font-semibold mb-2">Pledges</p>
          <p className="text-4xl font-bold text-blue-900">{pledges.length}</p>
        </div>
        <div className="bg-[#D1FAE5] rounded-xl p-6 border border-green-100">
          <p className="text-green-900 font-semibold mb-2">Money received by NGOs</p>
          <p className="text-4xl font-bold text-green-900">₹{totalMoney.toLocaleString()}</p>
        </div>
        <div className="bg-[#FEF3C7] rounded-xl p-6 border border-yellow-100">
          <p className="text-yellow-900 font-semibold mb-2">Tasks completed</p>
          <p className="text-4xl font-bold text-yellow-900">{tasksDone}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* My Pledges List */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-xl font-bold text-blue-900 mb-6">My Donations & Pledges</h2>
          <div className="space-y-4">
            {pledges.map((pledge) => (
              <div key={pledge.id} className="flex justify-between items-center border-b border-gray-100 pb-4 last:border-0 last:pb-0">
                <div>
                  <p className="font-bold text-gray-800">{pledge.campaign}</p>
                  <p className="text-sm text-gray-500">{pledge.detail}</p>
                </div>
                <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                  pledge.status === 'RECEIVED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                }`}>
                  {pledge.status}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* My Tasks List */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-xl font-bold text-blue-900 mb-6">My Volunteer Tasks</h2>
          <div className="space-y-4">
            {tasks.map((task) => (
              <div key={task.id} className="flex justify-between items-center border-b border-gray-100 pb-4 last:border-0 last:pb-0">
                <div>
                  <p className="font-bold text-gray-800">{task.title}</p>
                  <p className="text-sm text-gray-500">{task.campaign}</p>
                  {task.details && <p className="text-xs text-gray-400 mt-0.5">{task.details}</p>}
                </div>
                <div className="flex items-center gap-3">
                  <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                    task.status === 'DONE' 
                      ? 'bg-green-100 text-green-700' 
                      : task.status === 'ASSIGNED'
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-yellow-100 text-yellow-700'
                  }`}>
                    {task.status}
                  </span>
                  {task.status === 'ASSIGNED' && (
                    <button 
                      onClick={() => handleMarkDone(task.id)}
                      className="text-xs bg-gray-100 hover:bg-green-50 text-gray-700 hover:text-green-700 border border-gray-200 px-3 py-1.5 rounded-lg font-semibold transition"
                    >
                      Done ✓
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
