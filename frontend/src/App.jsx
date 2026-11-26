import Donate from './Donate';
import MyDonations from './MyDonations';
import AdminDonations from './AdminDonations';
import ManageCampaign from './ManageCampaign';
import ProtectedRoute from './ProtectedRoute';
import Layout from './Layout';
import LoginRegister from './LoginRegister';
import UserProfile from './UserProfile';
import DisasterList from './DisasterList';
import DisasterDetail from './DisasterDetail';
import FundUtilization from './FundUtilization';
import Settings from './Settings';
import NgoDashboard from './NgoDashboard';
import MyActivity from './MyActivity';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';

import VolunteerApplications from './VolunteerApplications';
import VolunteerAssignments from './VolunteerAssignments';
import VolunteerProfile from './VolunteerProfile';
import VolunteerMatching from './VolunteerMatching';
import Heatmap from './Heatmap';
import CampaignDetail from './CampaignDetail';
import Notifications from './Notifications';
import Reports from './Reports';
import Inventory from './Inventory';
import Beneficiaries from './Beneficiaries';

const Placeholder = ({ text }) => <div className="p-8 text-xl text-gray-500">{text}</div>;

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/disasters" element={<DisasterList />} />
          <Route path="/disaster/:id" element={<DisasterDetail />} />
          <Route path="/donate/:id" element={<Donate />} />
          <Route path="/my-donations" element={<MyDonations />} />
          <Route path="/admin-donations" element={<AdminDonations />} />
          <Route element={ <ProtectedRoute roles={['ADMIN']} /> }>
            <Route path="/admin-donations" element={<AdminDonations />} />
          </Route>
          <Route path="/profile" element={<UserProfile />} />
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginRegister />} />

          {/* Must be logged in */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              {/* All roles */}
              <Route path="/dashboard" element={<Heatmap />} />
              <Route path="/campaign/:id" element={<CampaignDetail />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="/notifications" element={<Notifications />} />

              {/* Old admin link now opens the combined NGO Panel */}
              <Route path="/admin" element={<Navigate to="/ngo" replace />} />

              {/* NGO and Admin (one combined panel) */}
              <Route element={<ProtectedRoute roles={['NGO', 'ADMIN']} />}>
                <Route path="/ngo" element={<NgoDashboard />} />
                <Route path="/manage-campaign" element={<ManageCampaign />} />
                <Route path="/volunteer-matching" element={<VolunteerMatching />} />
                <Route path="/volunteer-applications" element={<VolunteerApplications />} />
                <Route path="/volunteer-assignments" element={<VolunteerAssignments />} />
                <Route path="/volunteer-profile" element={<VolunteerProfile />} />
                <Route path="/inventory" element={<Inventory />} />
                <Route path="/beneficiaries" element={<Beneficiaries />} />
                <Route path="/reports" element={<Reports />} />
              </Route>

              {/* Citizen only */}
              <Route element={<ProtectedRoute roles={['CITIZEN']} />}>
                <Route path="/my-activity" element={<MyActivity />} />
              </Route>

              <Route path="*" element={<Placeholder text="Page under construction 🚧" />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;