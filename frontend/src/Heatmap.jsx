import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { ArrowRight, IndianRupee, CheckCircle2, Activity, Megaphone, Heart, Wheat, Bed } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import api from './api';
import bannerImage from './assets/banner-image.png';
import riceImg from './assets/rice.png';
import blanketImg from './assets/blanket.png';

export default function Heatmap() {
  const { user } = useAuth();
  const center = [17.0500, 74.2800]; 

  const [stats, setStats] = useState({
    activeDisasters: 3,
    volunteers: '1,250+',
    donationsToday: '₹8.5L'
  });

  const [campaignsList, setCampaignsList] = useState([]);

  useEffect(() => {
    // Load live platform stats
    api.get('/donations/stats')
      .then(res => {
        if (res) {
          setStats({
            activeDisasters: res.activeCampaignsCount || 3,
            volunteers: res.totalContributorsCount ? `${res.totalContributorsCount}+` : '1,250+',
            donationsToday: res.totalFundsCollected ? `₹${(res.totalFundsCollected / 100000).toFixed(1)}L` : '₹8.5L'
          });
        }
      })
      .catch(() => {});

    // Load active campaigns for the cards
    api.get('/campaigns?size=3')
      .then(res => {
        const list = res.content || (Array.isArray(res) ? res : []);
        if (list.length > 0) {
          setCampaignsList(list);
        }
      })
      .catch(() => {});
  }, []);

  return (
    <div className="pb-10 max-w-[1400px] mx-auto">
      
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-[#1E3A8A] border-l-4 border-blue-600 pl-4">
          Welcome, {user?.name || 'Friend'} 👋
        </h1>
        <p className="text-gray-600 mt-1 pl-4 font-medium">Together, we can rebuild lives and bring hope.</p>
      </div>

      {/* Top Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-[#FFF1F2] rounded-xl border border-red-100 p-5 flex items-center justify-between shadow-sm relative overflow-hidden">
          <div>
            <p className="text-gray-800 font-semibold text-sm">Monitored Disasters</p>
            <div className="flex items-end gap-2 mt-1">
              <p className="text-3xl font-bold text-red-600 leading-none">{stats.activeDisasters}</p>
              <span className="text-xs text-red-500 font-bold">Active</span>
            </div>
          </div>
          <div className="h-12 w-12 bg-white rounded-xl flex items-center justify-center text-red-500 shadow-sm text-2xl">⚠️</div>
        </div>
        
        <div className="bg-[#EFF6FF] rounded-xl border border-blue-100 p-5 flex items-center justify-between shadow-sm relative overflow-hidden">
          <div>
            <p className="text-gray-800 font-semibold text-sm">Active Relief Forces</p>
            <div className="flex items-end gap-2 mt-1">
              <p className="text-3xl font-bold text-blue-600 leading-none">{stats.volunteers}</p>
              <span className="text-xs text-green-600 font-bold">Verified</span>
            </div>
          </div>
          <div className="h-12 w-12 bg-white rounded-xl flex items-center justify-center text-blue-500 shadow-sm text-2xl">👥</div>
        </div>
        
        <div className="bg-[#ECFDF5] rounded-xl border border-green-100 p-5 flex items-center justify-between shadow-sm relative overflow-hidden">
          <div>
            <p className="text-gray-800 font-semibold text-sm">Total Funds Raised</p>
            <div className="flex items-end gap-2 mt-1">
              <p className="text-3xl font-bold text-green-600 leading-none">{stats.donationsToday}</p>
              <span className="text-xs text-green-600 font-bold">100% Verified</span>
            </div>
          </div>
          <div className="h-12 w-12 bg-white rounded-xl flex items-center justify-center text-green-500 shadow-sm text-2xl">💚</div>
        </div>
      </div>

      {/* Middle Section */}
      <div className="flex flex-col lg:flex-row gap-6 mb-8">
        
        {/* Satellite Map */}
        <div className="flex-1 bg-white rounded-xl shadow-sm border border-gray-100 p-4">
          <div className="flex justify-between items-center mb-4 pl-2 pr-2">
            <h2 className="text-lg font-bold text-[#1E3A8A] flex items-center gap-2">
              📍 Disaster Response Heatmap
            </h2>
            <span className="text-sm font-semibold text-green-600 flex items-center gap-1">
              <span className="h-2 w-2 bg-green-500 rounded-full animate-pulse"></span> Live
            </span>
          </div>
          
          <div className="h-[400px] rounded-lg overflow-hidden border border-gray-200 relative">
            <MapContainer center={center} zoom={8} style={{ height: '100%', width: '100%', background: '#0a192f' }}>
              <TileLayer url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}" />
              
              {/* Kolhapur */}
              <CircleMarker center={[16.7032, 74.2434]} radius={45} pathOptions={{ stroke: false, fillColor: '#EF4444', fillOpacity: 0.2 }} />
              <CircleMarker center={[16.7032, 74.2434]} radius={12} pathOptions={{ color: '#fff', weight: 2, fillColor: '#EF4444', fillOpacity: 1 }}><Popup>Severe Disaster: Kolhapur Floods</Popup></CircleMarker>

              {/* Sangli */}
              <CircleMarker center={[16.8524, 74.5815]} radius={35} pathOptions={{ stroke: false, fillColor: '#F59E0B', fillOpacity: 0.2 }} />
              <CircleMarker center={[16.8524, 74.5815]} radius={10} pathOptions={{ color: '#fff', weight: 2, fillColor: '#F59E0B', fillOpacity: 1 }}><Popup>Critical Warning: Sangli Region</Popup></CircleMarker>
            </MapContainer>
          </div>
        </div>

        {/* Urgent Campaign Spotlight Banner */}
        <div className="w-full lg:w-[420px] bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex flex-col justify-between">
          <div>
            <div className="h-44 relative overflow-hidden bg-slate-900">
              <img src={bannerImage} alt="Flood Alert" className="w-full h-full object-cover" />
              <span className="absolute top-3 left-3 bg-red-600 text-white text-xs font-bold px-3 py-1 rounded-full shadow-md uppercase tracking-wide">
                Urgent Relief Drive
              </span>
            </div>
            
            <div className="p-5">
              <h3 className="text-xl font-bold text-gray-900 leading-tight">
                {campaignsList[0]?.title || "Kolhapur Flood Relief 2026"}
              </h3>
              <p className="text-gray-600 text-sm mt-2 line-clamp-2">
                Emergency distribution of safe drinking water, ration kits, and medical aid underway.
              </p>
              
              <div className="mt-4">
                <div className="flex justify-between text-xs font-bold mb-1">
                  <span className="text-blue-900">Fundraising Target</span>
                  <span className="text-gray-500">65% Achieved</span>
                </div>
                <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                  <div className="bg-blue-600 h-full rounded-full" style={{ width: '65%' }} />
                </div>
              </div>
            </div>
          </div>
          
          <div className="p-5 pt-0">
            <Link
              to="/campaign-detail"
              className="w-full bg-[#1E3A8A] hover:bg-blue-800 text-white font-bold py-3 px-4 rounded-xl shadow-md transition flex items-center justify-center gap-2 text-sm"
            >
              Support Campaign <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </div>

      {/* Immediate Supplies Grid */}
      <div>
        <h2 className="text-xl font-bold text-[#1E3A8A] mb-4">Required Relief Resources</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 flex items-center gap-4">
            <div className="h-16 w-16 bg-amber-50 rounded-xl flex items-center justify-center p-2 border border-amber-100">
              <img src={riceImg} alt="Food Supplies" className="h-full w-full object-contain" />
            </div>
            <div className="flex-1">
              <div className="flex justify-between items-start">
                <h4 className="font-bold text-gray-800">Ration Kits & Dry Food</h4>
                <span className="text-xs font-bold bg-amber-100 text-amber-800 px-2 py-0.5 rounded">High Priority</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">Needed: 500 bags • Received: 350 bags</p>
              <div className="w-full bg-gray-100 h-1.5 rounded-full mt-2">
                <div className="bg-amber-500 h-full rounded-full" style={{ width: '70%' }} />
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 flex items-center gap-4">
            <div className="h-16 w-16 bg-blue-50 rounded-xl flex items-center justify-center p-2 border border-blue-100">
              <img src={blanketImg} alt="Shelter Supplies" className="h-full w-full object-contain" />
            </div>
            <div className="flex-1">
              <div className="flex justify-between items-start">
                <h4 className="font-bold text-gray-800">Warm Blankets & Tents</h4>
                <span className="text-xs font-bold bg-blue-100 text-blue-800 px-2 py-0.5 rounded">Urgent</span>
              </div>
              <p className="text-xs text-gray-500 mt-1">Needed: 200 units • Received: 80 units</p>
              <div className="w-full bg-gray-100 h-1.5 rounded-full mt-2">
                <div className="bg-blue-600 h-full rounded-full" style={{ width: '40%' }} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
