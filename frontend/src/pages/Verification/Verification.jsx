import React, { useState, useEffect } from 'react';
import {
  Award,
  ShieldCheck,
  ShieldAlert,
  Search,
  CheckCircle2,
  XCircle,
  QrCode,
  Key,
  Copy,
  Lock,
  RefreshCw,
  AlertTriangle
} from 'lucide-react';
import { credentialService } from '../../services/credentialService';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Input } from '../../components/ui/Input';
import { LoadingSpinner } from '../../components/feedback/LoadingSpinner';

export const Verification = () => {
  const { user } = useAuth();
  const { showToast } = useToast();

  const [credentialId, setCredentialId] = useState('');
  const [issuedCredentials, setIssuedCredentials] = useState([]);
  const [verificationResult, setVerificationResult] = useState(null);
  const [isVerifying, setIsVerifying] = useState(false);

  // Tamper test mode
  const [isTamperMode, setIsTamperMode] = useState(false);
  const [customPayload, setCustomPayload] = useState('');

  useEffect(() => {
    loadIssuedCredentials();
  }, []);

  const loadIssuedCredentials = async () => {
    try {
      const res = await credentialService.getCredentials({ size: 10 });
      setIssuedCredentials(res.content || []);
      if (res.content && res.content.length > 0) {
        setCredentialId(res.content[0].credentialId);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleVerify = async (credIdToVerify = credentialId, payloadOverride = null) => {
    const targetId = credIdToVerify || credentialId;
    if (!targetId.trim()) {
      showToast('Please enter a valid Credential ID', 'warning');
      return;
    }

    setIsVerifying(true);
    setVerificationResult(null);

    try {
      const body = {
        credentialId: targetId.trim()
      };

      if (isTamperMode && customPayload) {
        body.payloadJson = customPayload;
      }

      const res = await credentialService.verifyCredential(body);
      setVerificationResult(res);

      if (res.valid) {
        showToast('Cryptographic signature and credential validity confirmed!', 'success');
      } else {
        showToast(res.verificationNotes || 'Verification failed', 'error');
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Verification service error', 'error');
    } finally {
      setIsVerifying(false);
    }
  };

  const handleSelectSample = (cred) => {
    setCredentialId(cred.credentialId);
    setVerificationResult(null);
    setIsTamperMode(false);
    setCustomPayload('');
  };

  const handleEnableTamperTest = (cred) => {
    setCredentialId(cred.credentialId);
    setIsTamperMode(true);
    // Altered payload to demonstrate cryptographic signature failure
    const tampered = JSON.stringify({
      completionYear: cred.completionYear,
      credentialId: cred.credentialId,
      institutionCode: 'AICTE-DEL-101',
      issueDate: cred.issueDate,
      qualification: 'Doctor of Science (Honorary & Falsified)', // Tampered claim!
      syntheticSubjectRef: cred.syntheticSubjectRef
    });
    setCustomPayload(tampered);
    showToast('Tampered payload loaded for demonstration. Click Verify to test signature failure.', 'info');
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-5xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900">Cryptographic Credential Verification Portal</h2>
        <p className="text-xs text-slate-500 mt-0.5">
          Authenticate student credentials with RSA-2048 digital signatures and minimal necessary disclosure
        </p>
      </div>

      {/* Verification Input Box */}
      <Card className="p-6">
        <div className="max-w-2xl mx-auto space-y-4">
          <label className="block text-xs font-semibold text-slate-700">
            Enter AICTE Credential Reference Identifier
          </label>
          <div className="flex gap-2">
            <Input
              value={credentialId}
              onChange={(e) => setCredentialId(e.target.value)}
              placeholder="e.g. AICTE-CRED-9307E111"
              icon={Search}
              className="flex-1"
            />
            <Button
              onClick={() => handleVerify()}
              isLoading={isVerifying}
              icon={ShieldCheck}
            >
              Verify Credential
            </Button>
          </div>

          {/* Tamper mode toggle box */}
          {isTamperMode && (
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl space-y-2 text-xs">
              <div className="flex items-center justify-between text-rose-900 font-bold">
                <span className="flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4 text-rose-600" />
                  Simulated Tamper Test Mode Active
                </span>
                <button
                  type="button"
                  onClick={() => setIsTamperMode(false)}
                  className="text-slate-500 hover:text-slate-700 underline text-[11px]"
                >
                  Reset to Authentic
                </button>
              </div>
              <p className="text-rose-700 text-[11px]">
                The claim below has been modified by an adversary. Running verification will trigger RSA signature mismatch.
              </p>
              <textarea
                value={customPayload}
                onChange={(e) => setCustomPayload(e.target.value)}
                rows={3}
                className="w-full font-mono text-[11px] p-2 bg-white rounded border border-rose-200 text-slate-800"
              />
            </div>
          )}
        </div>
      </Card>

      {/* Verification Result Card */}
      {verificationResult && (
        <Card
          className={`border-2 animate-scale-up ${
            verificationResult.valid ? 'border-emerald-300 bg-emerald-50/20' : 'border-rose-300 bg-rose-50/20'
          }`}
        >
          <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div className="flex items-center gap-3">
              <div
                className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${
                  verificationResult.valid
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'bg-rose-100 text-rose-700'
                }`}
              >
                {verificationResult.valid ? (
                  <CheckCircle2 className="w-7 h-7" />
                ) : (
                  <XCircle className="w-7 h-7" />
                )}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-base font-bold text-slate-900">
                    {verificationResult.valid ? 'Authentic Credential Verified' : 'Verification Check Failed'}
                  </h3>
                  <Badge variant={verificationResult.credentialStatus}>
                    {verificationResult.credentialStatus}
                  </Badge>
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  Reference: <span className="font-mono font-semibold text-slate-800">{verificationResult.credentialId}</span>
                </p>
              </div>
            </div>

            <div className="text-right text-xs text-slate-500">
              <p>Algorithm: <span className="font-mono text-slate-700 font-semibold">{verificationResult.algorithm}</span></p>
              <p className="text-[10px] text-slate-400">Verified at: {new Date(verificationResult.verifiedAt).toLocaleTimeString()}</p>
            </div>
          </div>

          {/* Minimal Necessary Disclosure Claims Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
            <div className="p-4 bg-white rounded-xl border border-slate-200/80 space-y-2.5 text-xs">
              <h4 className="font-bold text-slate-900 uppercase text-[11px] tracking-wider text-slate-500">
                Verified Academic Claims
              </h4>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Awarded Qualification:</span>
                <span className="font-bold text-slate-900">{verificationResult.qualification || 'N/A'}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Issuer Institution:</span>
                <span className="font-semibold text-slate-800">{verificationResult.institutionName || 'N/A'}</span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Completion Year:</span>
                <span className="font-semibold text-slate-800">{verificationResult.completionYear || 'N/A'}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-500">Date of Award:</span>
                <span className="text-slate-800">{verificationResult.issueDate || 'N/A'}</span>
              </div>
            </div>

            <div className="p-4 bg-white rounded-xl border border-slate-200/80 space-y-2.5 text-xs">
              <h4 className="font-bold text-slate-900 uppercase text-[11px] tracking-wider text-slate-500">
                {verificationResult.studentName ? 'Authoritative Student Identification' : 'Privacy Protection (Minimal Disclosure)'}
              </h4>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">
                  {verificationResult.studentName ? 'Full Student Name:' : 'Masked Student Name:'}
                </span>
                <span className="font-bold text-slate-900 font-medium">
                  {verificationResult.studentName ? (
                    <span className="text-blue-900 bg-blue-50 px-2 py-0.5 rounded border border-blue-200 font-bold">
                      {verificationResult.studentName}
                    </span>
                  ) : (
                    <span className="font-mono text-slate-700">{verificationResult.maskedStudentName}</span>
                  )}
                </span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Synthetic Subject Reference:</span>
                <span className="font-mono font-semibold text-blue-700 bg-blue-50 px-1.5 py-0.5 rounded">
                  {verificationResult.syntheticSubjectRef}
                </span>
              </div>
              <div className="flex justify-between py-1 border-b border-slate-100">
                <span className="text-slate-500">Signature Mathematically Valid:</span>
                <span className={`font-bold ${verificationResult.signatureValid ? 'text-emerald-600' : 'text-rose-600'}`}>
                  {verificationResult.signatureValid ? 'YES (Cryptographically Valid)' : 'NO (Failed / Tampered)'}
                </span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-slate-500">Credential Status / Access:</span>
                <Badge variant={verificationResult.valid ? 'success' : 'danger'}>
                  {verificationResult.credentialStatus}
                </Badge>
              </div>
            </div>
          </div>

          {/* Notes */}
          <div className="mt-4 p-3 bg-slate-50 rounded-lg text-xs text-slate-600 flex items-start gap-2">
            <Lock className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
            <p>{verificationResult.verificationNotes}</p>
          </div>
        </Card>
      )}

      {/* Test Bench Samples Table */}
      <Card
        title="Sample Digitally Signed Credentials in Registry"
        subtitle="Click any sample to verify genuine signature, or click 'Test Tamper' to prove cryptographic protection"
      >
        <div className="overflow-x-auto rounded-lg border border-slate-200">
          <table className="min-w-full divide-y divide-slate-200 text-left text-xs">
            <thead className="bg-slate-50 font-semibold text-slate-700">
              <tr>
                <th className="px-4 py-3">Credential ID</th>
                <th className="px-4 py-3">Qualification</th>
                <th className="px-4 py-3">Synthetic Subject</th>
                <th className="px-4 py-3">Issuer</th>
                <th className="px-4 py-3">Year</th>
                <th className="px-4 py-3 text-right">Verification Tests</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 bg-white text-slate-600">
              {issuedCredentials.map((cred) => (
                <tr key={cred.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3 font-mono font-bold text-slate-900">{cred.credentialId}</td>
                  <td className="px-4 py-3 font-medium text-slate-800">{cred.qualification}</td>
                  <td className="px-4 py-3 font-mono text-blue-700">{cred.syntheticSubjectRef}</td>
                  <td className="px-4 py-3">{cred.issuer}</td>
                  <td className="px-4 py-3">{cred.completionYear}</td>
                  <td className="px-4 py-3 text-right whitespace-nowrap space-x-2">
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => {
                        handleSelectSample(cred);
                        handleVerify(cred.credentialId);
                      }}
                    >
                      Verify Genuine
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => handleEnableTamperTest(cred)}
                    >
                      Simulate Tamper
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};
